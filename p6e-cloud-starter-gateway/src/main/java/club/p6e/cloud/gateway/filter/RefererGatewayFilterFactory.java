package club.p6e.cloud.gateway.filter;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Referer 过滤器网关
 *
 * @author lidashuang
 * @version 1.0
 */
public class RefererGatewayFilterFactory
        extends AbstractGatewayFilterFactory<RefererGatewayFilterFactory.Config> {

    /**
     * 顺序
     */
    private static final int ORDER = -1100;

    @Override
    public GatewayFilter apply(RefererGatewayFilterFactory.Config config) {
        return new CustomRefererGatewayFilter(config);
    }

    /**
     * 网关过滤器
     */
    public static class CustomRefererGatewayFilter implements GatewayFilter, Ordered {

        /**
         * REFERER
         */
        private static final String REFERER_HEADER = "Referer";

        /**
         * REFERER 通用内容
         */
        private static final String REFERER_HEADER_GENERAL_CONTENT = "*";

        /**
         * 错误结果内容
         */
        private static final String ERROR_RESULT_CONTENT =
                "{\"code\":403,\"message\":\"Forbidden\",\"data\":\"Referer Exception\"}";

        /**
         * 配置文件对象
         */
        private final Config config;

        /**
         * 构造方法初始化
         *
         * @param config 配置文件对象
         */
        public CustomRefererGatewayFilter(Config config) {
            this.config = config;
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            final ServerHttpRequest request = exchange.getRequest();
            final ServerHttpResponse response = exchange.getResponse();
            final List<String> refererList = request.getHeaders().get(REFERER_HEADER);

            if (refererList != null && !refererList.isEmpty()) {
                final String r = refererList.get(0);
                final String referer = r == null ? "" : r;
                for (final String item : config.getWhiteList()) {
                    if (REFERER_HEADER_GENERAL_CONTENT.equals(item) || referer.startsWith(item)) {
                        return chain.filter(exchange);
                    }
                }
            }

            response.setStatusCode(HttpStatus.FORBIDDEN);
            return response.writeWith(Mono.just(response.bufferFactory()
                    .wrap(ERROR_RESULT_CONTENT.getBytes(StandardCharsets.UTF_8))));
        }

        @Override
        public int getOrder() {
            return ORDER;
        }
    }

    @Data
    @Accessors(chain = true)
    public static class Config implements Serializable {
        private List<String> whiteList;
    }

}
