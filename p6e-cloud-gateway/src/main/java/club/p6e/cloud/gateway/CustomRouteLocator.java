package club.p6e.cloud.gateway;

import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author lidashuang
 * @version 1.0
 */
@Component
public class CustomRouteLocator implements RouteDefinitionRepository {

    /**
     * 事件创建对象
     */
    private final ApplicationEventPublisher publisher;

    /**
     * 路由对象
     */
    private final List<RouteDefinition> routeDefinitions = new ArrayList<>();

    /**
     * 构造方法初始化
     *
     * @param publisher 事件创建器
     */
    public CustomRouteLocator(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * 刷新路由
     *
     * @param routeDefinitions 路由定义对象
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
