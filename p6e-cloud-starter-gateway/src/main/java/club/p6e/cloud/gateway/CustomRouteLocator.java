package club.p6e.cloud.gateway;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Custom Route Locator
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = CustomRouteLocator.class,
        ignored = CustomRouteLocator.class
)
public class CustomRouteLocator implements RouteDefinitionRepository {

    /**
     * ApplicationEventPublisher object
     */
    private final ApplicationEventPublisher publisher;

    /**
     * RouteDefinitionList object
     */
    private final List<RouteDefinition> routeDefinitions = new ArrayList<>();

    /**
     * Constructor initializers
     *
     * @param publisher ApplicationEventPublisher object
     */
    public CustomRouteLocator(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * Refresh Route Definitions
     *
     * @param routeDefinitions RouteDefinitionList object
     */
    public void refresh(List<RouteDefinition> routeDefinitions) {
        this.routeDefinitions.clear();
        this.routeDefinitions.addAll(routeDefinitions);
        this.publisher.publishEvent(new RefreshRoutesEvent(this));
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return Flux.fromIterable(routeDefinitions);
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> mr) {
        return mr.map(routeDefinitions::add).then();
    }

    @Override
    public Mono<Void> delete(Mono<String> ms) {
        return ms.map(r -> {
            for (final RouteDefinition item : routeDefinitions) {
                if (item.getId().equals(r)) {
                    routeDefinitions.remove(item);
                    break;
                }
            }
            return r;
        }).then();
    }
}
