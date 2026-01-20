package club.p6e.cloud.gateway.auth;

import club.p6e.coat.common.controller.BaseWebFluxController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Auth Gateway Service
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
     * P6e User Auth Header Name
     */
    @SuppressWarnings("ALL")
    private static final String USER_AUTH_HEADER = "P6e-User-Auth";

    /**
     * P6e User Info Header Name
     */
    @SuppressWarnings("ALL")
    private static final String USER_INFO_HEADER = "P6e-User-Info";

    /**
     * AuthGatewayCache object
     */
    private final AuthGatewayCache cache;
    private final AuthParamHandler handler;

    /**
     * Constructor initializers
     *
     * @param cache AuthGatewayCache object
     */
    public AuthGatewayService(AuthGatewayCache cache, AuthParamHandler handler) {
        this.cache = cache;
        this.handler = handler;
    }

    /**
     * Authentication
     *
     * @param exchange ServerWebExchange object
     * @return Mono<ServerWebExchange> ServerWebExchange object
     */
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
            final String ft = token;
            return handler.execute(exchange)
                    .flatMap(r -> cache
                            .getAccessToken(ft, r)
                            .flatMap(t -> cache.refresh(t, r))
                            .flatMap(t -> cache.getUser(t.getUid(), r))
                            .flatMap(u -> Mono.just(exchange.mutate().request(
                                    exchange.getRequest().mutate()
                                            .header(USER_INFO_HEADER, u)
                                            .header(USER_AUTH_HEADER, "1")
                                            .build()
                            ).build()))
                            .switchIfEmpty(Mono.defer(() -> Mono.just(exchange.mutate().request(
                                    exchange.getRequest().mutate().header(USER_AUTH_HEADER, "1").build()
                            ).build())))
                    );
        }
    }

}
