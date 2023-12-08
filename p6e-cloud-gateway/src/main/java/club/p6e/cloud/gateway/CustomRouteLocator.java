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

    /**
     * 路由对象
     */
    private Flux<Route> routes;

    /**
     * 事件创建对象
     */
    private final ApplicationEventPublisher publisher;

    public CustomRouteLocator(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * 刷新路由
     *
     * @param routeDefinitions 路由定义对象
     */
    public void refresh(List<RouteDefinition> routeDefinitions) {
        // 创建路由对象
        routes = Flux
                .fromIterable(routeDefinitions)
                .map(rd -> Route.builder(rd).build());
        // 触发刷新路由事件
        publisher.publishEvent(new RefreshRoutesEvent(this));
    }

    @Override
    public Flux<Route> getRoutes() {
        return routes == null ? Flux.empty() : routes;
    }

}
