package club.p6e.cloud.gateway;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lidashuang
 * @version 1.0
 */
@Component
@Configuration
public class PropertiesRefresher {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesRefresher.class);

    public static class ReadyEventListener implements ApplicationListener<ApplicationReadyEvent> {

        private final PropertiesRefresher refresher;

        public ReadyEventListener(PropertiesRefresher refresher) {
            this.refresher = refresher;
        }

        @Override
        public void onApplicationEvent(@Nonnull ApplicationReadyEvent event) {
            refresher.init();
        }
    }

    public static class ContextClosedEventListener implements ApplicationListener<ContextClosedEvent> {

        private final PropertiesRefresher refresher;

        public ContextClosedEventListener(PropertiesRefresher refresher) {
            this.refresher = refresher;
        }

        @Override
        public void onApplicationEvent(@Nonnull ContextClosedEvent event) {
            refresher.close();
        }
    }

    @Bean
    public ReadyEventListener injectReadyEventListener(PropertiesRefresher refresher) {
        return new ReadyEventListener(refresher);
    }

    @Bean
    public ContextClosedEventListener injectContextClosedEventListener(PropertiesRefresher refresher) {
        return new ContextClosedEventListener(refresher);
    }

    private final Properties properties;
    private final CustomRouteLocator locator;
    private final ConnectionFactory databaseFactory;
    private final ReactiveRedisMessageListenerContainer container;

    public PropertiesRefresher(
            Properties properties,
            CustomRouteLocator locator,
            ConnectionFactory databaseFactory,
            ReactiveRedisConnectionFactory redisFactory
    ) {
        final ReactiveRedisMessageListenerContainer container =
                new ReactiveRedisMessageListenerContainer(redisFactory);
        this.locator = locator;
        this.container = container;
        this.properties = properties;
        this.databaseFactory = databaseFactory;
    }

    public void init() {
        container
                .receive(new ChannelTopic("p6e-cloud-gateway-config-topic"))
                .doOnNext(message -> execute())
                .subscribe();
    }

    public void close() {
        container.destroy();
    }

    @SuppressWarnings("ALL")
    private static final String SELECT_SQL = "" +
            "  SELECT  " +
            "  `id`, `type`, `key`, `value`  " +
            "  FROM `p6e_config`  " +
            "  WHERE `p6e_config`.`type` = 'club.p6e.cloud.gateway.properties'  " +
            "  ;  ";

    public void execute() {
        execute(Mono.usingWhen(
                databaseFactory.create(),
                connection -> Flux
                        .from(connection.createStatement(SELECT_SQL).execute())
                        .flatMap(r -> Mono.from(r.map((row, metadata) -> new HashMap<String, String>() {{
                            put("id", String.valueOf(row.get("id", Integer.class)));
                            put("key", row.get("key", String.class));
                            put("value", row.get("value", String.class));
                        }})))
                        .collectList()
                        .map(list -> {
                            final Map<String, String> result = new HashMap<>();
                            for (final Map<String, String> item : list) {
                                result.put(item.get("key"), item.get("value"));
                            }
                            return result;
                        }),
                Connection::close
        ).block());
    }

    public void execute(Map<String, String> data) {
        if (data != null) {
            if (data.get("p6e.cloud.gateway.version") != null) {
                this.properties.setVersion(data.get("p6e.cloud.gateway.version"));
            }

            if (data.get("p6e.cloud.gateway.log.details") != null) {
                this.properties.getLog().setDetails(
                        "true".equalsIgnoreCase(data.get("p6e.cloud.gateway.log.details")));
            }

            if (data.get("p6e.cloud.gateway.referer.enabled") != null) {
                this.properties.getReferer().setEnabled(
                        "true".equalsIgnoreCase(data.get("p6e.cloud.gateway.referer.enabled")));
            }

            if (data.get("p6e.cloud.gateway.referer.white-list") != null) {
                final List<String> list = JsonUtil.fromJsonToList(
                        data.get("p6e.cloud.gateway.referer.white-list"), String.class);
                this.properties.getReferer().setWhiteList(list == null ? new String[0] : list.toArray(new String[0]));
            }

            if (data.get("p6e.cloud.gateway.cross-domain.enabled") != null) {
                this.properties.getCrossDomain().setEnabled(
                        "true".equalsIgnoreCase(data.get("p6e.cloud.gateway.cross-domain.enabled")));
            }

            if (data.get("p6e.cloud.gateway.request-header-clear") != null) {
                final List<String> list = JsonUtil.fromJsonToList(
                        data.get("p6e.cloud.gateway.request-header-clear"), String.class);
                this.properties.setRequestHeaderClear(list == null ? new ArrayList<>() : list);
            }

            if (data.get("p6e.cloud.gateway.response-header-only") != null) {
                final List<String> list = JsonUtil.fromJsonToList(
                        data.get("p6e.cloud.gateway.response-header-only"), String.class);
                this.properties.setResponseHeaderOnly(list == null ? new ArrayList<>() : list);
            }

            if (data.get("p6e.cloud.gateway.routes") != null) {
                locator.refresh(getRouteDefinition(data.get("p6e.cloud.gateway.routes")));
            }
        }
    }

    @SuppressWarnings("ALL")
    private List<RouteDefinition> getRouteDefinition(String content) {
        final List<RouteDefinition> list = new ArrayList<>();
        final Map<String, Object> routes = JsonUtil.fromJsonToMap(content, String.class, Object.class);
        if (routes != null) {
            for (final String key : routes.keySet()) {
                try {
                    final Object o = routes.get(key);
                    if (o instanceof Map) {
                        final Map<String, Object> map = (Map<String, Object>) o;
                        final RouteDefinition definition = new RouteDefinition();
                        definition.setId(key);
                        definition.setOrder(map.get("order") == null
                                ? 0 : Integer.parseInt(String.valueOf(map.get("order"))));
                        definition.setUri(new URI(String.valueOf(map.get("uri"))));
                        definition.setMetadata(map.get("metadata") == null
                                ? new HashMap<>() : (Map<String, Object>) map.get("metadata"));
                        if (map.get("filters") instanceof String) {
                            final List<String> filters = JsonUtil.fromJsonToList(
                                    String.valueOf(map.get("filters")), String.class);
                            if (filters != null) {
                                definition.setFilters(filters.stream().map(
                                        item -> new FilterDefinition(item)).toList());
                            }
                        }
                        if (map.get("predicates") instanceof String) {
                            final List<String> predicates = JsonUtil.fromJsonToList(
                                    String.valueOf(map.get("predicates")), String.class);
                            if (predicates != null) {
                                definition.setPredicates(predicates.stream().map(
                                        item -> new PredicateDefinition(item)).toList());
                            }
                        }
                        list.add(definition);
                    }
                } catch (Exception e) {
                    LOGGER.error("[P6E GATEWAY ROUTES ERROR]", e);
                }
            }
        }
        return list;
    }

}
