package club.p6e.cloud.gateway.filter;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Cross Domain 过滤器网关
 *
 * @author lidashuang
 * @version 1.0
 */
public class CrossDomainGatewayFilterFactory
        extends AbstractGatewayFilterFactory<CrossDomainGatewayFilterFactory.Config> {

    /**
     * 顺序
     */
    private static final int ORDER = -1200;

    @Override
    public GatewayFilter apply(CrossDomainGatewayFilterFactory.Config config) {
        return new CustomCrossDomainGatewayFilter(config);
    }

    /**
     * 网关过滤器
     */
    public static class CustomCrossDomainGatewayFilter implements GatewayFilter, Ordered {

        /**
         * 通用内容
         */
        private static final String CROSS_DOMAIN_HEADER_GENERAL_CONTENT = "*";

        /**
         * 跨域配置 ACCESS_CONTROL_MAX_AGE
         */
        private static final long ACCESS_CONTROL_MAX_AGE = 3600L;

        /**
         * 跨域配置 ACCESS_CONTROL_ALLOW_ORIGIN
         */
        private static final boolean ACCESS_CONTROL_ALLOW_CREDENTIALS = true;

        /**
         * 跨域配置 ACCESS_CONTROL_ALLOW_HEADERS
         */
        private static final String[] ACCESS_CONTROL_ALLOW_HEADERS = new String[]{
                "Accept",
                "Host",
                "Origin",
                "Referer",
                "User-Agent",
                "Content-Type",
                "Authorization"
        };

        /**
         * 跨域配置 ACCESS_CONTROL_ALLOW_METHODS
         */
        private static final HttpMethod[] ACCESS_CONTROL_ALLOW_METHODS = new HttpMethod[]{
                HttpMethod.GET,
                HttpMethod.POST,
                HttpMethod.PUT,
                HttpMethod.DELETE,
                HttpMethod.OPTIONS,
        };

        /**
         * 错误结果内容
         */
        private static final String ERROR_RESULT_CONTENT =
                "{\"code\":403,\"message\":\"Forbidden\",\"data\":\"Cross Domain Exception\"}";

        /**
         * 配置文件对象
         */
        private final Config config;

        /**
         * 构造方法初始化
         *
         * @param config 配置文件对象
         */
        public CustomCrossDomainGatewayFilter(Config config) {
            this.config = config;
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            final ServerHttpRequest request = exchange.getRequest();
            final ServerHttpResponse response = exchange.getResponse();

            final String origin = request.getHeaders().getOrigin();
            if (origin == null) {
                response.setStatusCode(HttpStatus.FORBIDDEN);
                return response.writeWith(Mono.just(response.bufferFactory()
                        .wrap(ERROR_RESULT_CONTENT.getBytes(StandardCharsets.UTF_8))));
            }

            boolean status = false;
            for (final String item : config.getWhiteList()) {
                if (CROSS_DOMAIN_HEADER_GENERAL_CONTENT.equals(item) || origin.startsWith(item)) {
                    status = true;
                    break;
                }
            }

            if (status) {
                response.getHeaders().setAccessControlAllowOrigin(origin);
                response.getHeaders().setAccessControlMaxAge(getAccessControlMaxAge());
                response.getHeaders().setAccessControlAllowCredentials(getAccessControlAllowCredentials());
                response.getHeaders().setAccessControlAllowHeaders(Arrays.asList(getAccessControlAllowHeaders()));
                response.getHeaders().setAccessControlAllowMethods(Arrays.asList(getAccessControlAllowMethods()));

                if (HttpMethod.OPTIONS.matches(request.getMethod().name().toUpperCase())) {
                    response.setStatusCode(HttpStatus.NO_CONTENT);
                    return Mono.empty();
                } else {
                    return chain.filter(exchange);
                }
            } else {
                response.setStatusCode(HttpStatus.FORBIDDEN);
                return response.writeWith(Mono.just(response.bufferFactory()
                        .wrap(ERROR_RESULT_CONTENT.getBytes(StandardCharsets.UTF_8))));
            }
        }

        /**
         * ACCESS_CONTROL_MAX_AGE
         *
         * @return ACCESS_CONTROL_MAX_AGE
         */
        private long getAccessControlMaxAge() {
            return ACCESS_CONTROL_MAX_AGE;
        }

        /**
         * ACCESS_CONTROL_ALLOW_METHODS
         *
         * @return ACCESS_CONTROL_ALLOW_METHODS
         */
        private HttpMethod[] getAccessControlAllowMethods() {
            return ACCESS_CONTROL_ALLOW_METHODS;
        }

        /**
         * ACCESS_CONTROL_ALLOW_HEADERS
         *
         * @return ACCESS_CONTROL_ALLOW_HEADERS
         */
        private String[] getAccessControlAllowHeaders() {
            return ACCESS_CONTROL_ALLOW_HEADERS;
        }

        /**
         * ACCESS_CONTROL_ALLOW_CREDENTIALS
         *
         * @return ACCESS_CONTROL_ALLOW_CREDENTIALS
         */
        private boolean getAccessControlAllowCredentials() {
            return ACCESS_CONTROL_ALLOW_CREDENTIALS;
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
