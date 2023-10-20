package club.p6e.cloud.gateway;

import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @author lidashuang
 * @version 1.0
 */
@Component
public class CustomRouteLocator implements RouteLocator {

    private Flux<Route> routes;
    private final ApplicationEventPublisher publisher;

    public CustomRouteLocator(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void refresh(List<RouteDefinition> routeDefinitions) {
        routes = Flux
                .fromIterable(routeDefinitions)
                .map(rd -> Route.builder(rd).build());
        publisher.publishEvent(new RefreshRoutesEvent(this));
    }

    @Override
    public Flux<Route> getRoutes() {
        return routes == null ? Flux.empty() : routes;
    }
}
