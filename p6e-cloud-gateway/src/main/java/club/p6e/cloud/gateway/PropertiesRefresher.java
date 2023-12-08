package club.p6e.cloud.gateway;

import club.p6e.coat.common.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

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

    /**
     * 注入日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesRefresher.class);

    /**
     * 配置文件对象
     */
    private final Properties properties;

    /**
     * 自定义路由定位器
     */
    private final CustomRouteLocator locator;

    /**
     * 构造方法初始化
     *
     * @param properties 配置文件对象
     * @param locator    路由初始化定位器
     */
    public PropertiesRefresher(Properties properties, CustomRouteLocator locator) {
        this.locator = locator;
        this.properties = properties;
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
