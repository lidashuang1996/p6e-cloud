package club.p6e.cloud.gateway.filter;

import club.p6e.coat.common.controller.BaseWebFluxController;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 凭证过滤器网关
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
public class LanguageGatewayFilterFactory
        extends AbstractGatewayFilterFactory<Object> {

    /**
     * 默认语言
     */
    public static String DEFAULT_LANGUAGE = "zh-cn";

    /**
     * 顺序
     */
    private static final int ORDER = -960;

    /**
     * 用户语言请求参数
     */
    private static final String LANGUAGE_PARAM = "language";

    /**
     * 用户请求语言信息的头部
     */
    @SuppressWarnings("ALL")
    private static final String X_LANGUAGE_HEADER = "X-Language";

    /**
     * 用户语言信息的头部
     */
    @SuppressWarnings("ALL")
    private static final String USER_LANGUAGE_HEADER = "P6e-Language";

    @Override
    public GatewayFilter apply(Object object) {
        return new CustomGatewayFilter();
    }

    /**
     * 网关过滤器
     */
    public static class CustomGatewayFilter implements GatewayFilter, Ordered {

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            final ServerHttpRequest request = exchange.getRequest();
            final String language = getLanguage(request);
            return chain.filter(exchange.mutate().request(
                    request.mutate().header(USER_LANGUAGE_HEADER,
                            language == null ? DEFAULT_LANGUAGE : language).build()
            ).build());
        }

        @Override
        public int getOrder() {
            return ORDER;
        }

        /**
         * 获取请求的语言信息
         *
         * @param request 请求对象
         * @return 语言信息
         */
        private String getLanguage(ServerHttpRequest request) {
            final String language = BaseWebFluxController.getParam(request, LANGUAGE_PARAM);
            if (language == null) {
                return BaseWebFluxController.getHeader(request, X_LANGUAGE_HEADER);
            } else {
                return language;
            }
        }

    }

}
