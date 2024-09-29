package club.p6e.cloud.gateway.auth;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Auth Gateway Filter Factory
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    /**
     * AuthGatewayService object
     */
    private final AuthGatewayService service;

    /**
     * Constructor initializers
     *
     * @param service AuthGatewayService object
     */
    public AuthGatewayFilterFactory(AuthGatewayService service) {
        this.service = service;
    }

    @Override
    public GatewayFilter apply(Object config) {
        return new CustomGatewayFilter(service);
    }

    /**
     * CustomGatewayFilter
     *
     * @param service AuthGatewayService object
     */
    private record CustomGatewayFilter(AuthGatewayService service) implements GatewayFilter {

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            return service.execute(exchange).flatMap(chain::filter);
        }

    }

}
