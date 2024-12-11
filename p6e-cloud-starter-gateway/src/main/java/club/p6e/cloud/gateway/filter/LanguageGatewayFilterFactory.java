package club.p6e.cloud.gateway.filter;

import club.p6e.coat.common.controller.BaseWebFluxController;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Language Gateway Filter Factory
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
public class LanguageGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    /**
     * Language Param Name
     */
    private static final String LANGUAGE_PARAM = "language";

    /**
     * Language Header Name
     */
    @SuppressWarnings("ALL")
    private static final String X_LANGUAGE_HEADER = "X-Language";

    /**
     * P6e Language Header Name
     */
    @SuppressWarnings("ALL")
    private static final String USER_LANGUAGE_HEADER = "P6e-Language";

    @Override
    public GatewayFilter apply(Object object) {
        return new CustomGatewayFilter();
    }

    /**
     * Custom Gateway Filter
     */
    public static class CustomGatewayFilter implements GatewayFilter {

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            final ServerHttpRequest request = exchange.getRequest();
            final String language = obtainLanguage(request);
            if (language == null) {
                return chain.filter(exchange);
            } else {
                return chain.filter(exchange.mutate().request(request.mutate().header(USER_LANGUAGE_HEADER, language).build()).build());
            }
        }

        /**
         * Obtain language information from request
         *
         * @param request ServerHttpRequest object
         * @return Language information
         */
        private String obtainLanguage(ServerHttpRequest request) {
            final String language = BaseWebFluxController.getParam(request, LANGUAGE_PARAM);
            if (language == null) {
                return BaseWebFluxController.getHeader(request, X_LANGUAGE_HEADER);
            } else {
                return language;
            }
        }

    }

}
