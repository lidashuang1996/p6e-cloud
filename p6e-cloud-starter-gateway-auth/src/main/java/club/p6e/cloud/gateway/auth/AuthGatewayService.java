package club.p6e.cloud.gateway.auth;

import club.p6e.coat.common.controller.BaseWebFluxController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关认证服务类
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = AuthGatewayService.class,
        ignored = AuthGatewayService.class
)
public class AuthGatewayService {

    /**
     * 用户的信息头
     */
    @SuppressWarnings("ALL")
    private static final String USER_INFO_HEADER = "P6e-User-Info";

    /**
     * 认证缓存对象
     */
    private final AuthGatewayCache cache;

    /**
     * 构造方法注入
     *
     * @param cache 认证缓存对象
     */
    public AuthGatewayService(AuthGatewayCache cache) {
        this.cache = cache;
    }

    public Mono<ServerWebExchange> execute(ServerWebExchange exchange) {
        final ServerHttpRequest request = exchange.getRequest();
        String token = BaseWebFluxController.getHeaderToken(request);
        if (token == null) {
            token = BaseWebFluxController.getAccessToken(request);
        }
        if (token == null) {
            token = BaseWebFluxController.getCookieAccessToken(request);
        }
        if (token == null) {
            return Mono.empty();
        } else {
            return cache
                    .getAccessToken(token)
                    .flatMap(t -> cache.getUser(t.getUid()))
                    .flatMap(u -> Mono.just(exchange.mutate().request(
                            exchange.getRequest().mutate().header(USER_INFO_HEADER, u).build()
                    ).build()));
        }
    }

}
