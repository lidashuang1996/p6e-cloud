package club.p6e.cloud.gateway.auth;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lidashuang
 * @version 1.0
 */
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    /**
     * 顺序（越小越先被执行）
     */
    private static final int ORDER = -1000;

    @Override
    public GatewayFilter apply(Object config) {
        return new CustomGatewayFilter();
    }

    /**
     * 自定义网关过滤器
     */
    private static class CustomGatewayFilter implements GatewayFilter, Ordered {

        @Data
        @Accessors(chain = true)
        private static class UserInfo implements Serializable {
            private List<String> group = new ArrayList<>();
        }

        /**
         * 格式化时间对象
         */
        private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        /**
         * 权限异常结果统一返回内容
         *
         * @param exchange 参数 ServerWebExchange 对象
         * @return Mono<Void> 请求结果直接返回
         */
        private static Mono<Void> exceptionErrorResult(ServerWebExchange exchange) {
            final ServerHttpRequest request = exchange.getRequest();
            final ServerHttpResponse response = exchange.getResponse();
            final String result = "{\"timestamp\":\"" + DATE_TIME_FORMATTER.format(LocalDateTime.now()) + "\",\"path\":\""
                    + request.getPath() + "\",\"message\":\"No Auth\",\"requestId\":\"" + request.getId() + "\",\"code\":401}";
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(result.getBytes(StandardCharsets.UTF_8))));
        }

        @SuppressWarnings("ALL")
        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//            final ServerHttpRequest request = exchange.getRequest();
//            final String path = request.getPath().value();
//            final String method = request.getMethod().name().toUpperCase();
//            final String uic = request.getHeaders().getFirst("P6e-User-Info");
//            final UserInfo userInfo = uic == null ? null : JsonUtil.fromJson(uic, UserInfo.class);
//            return Mono.defer(() -> userInfo == null ? Mono.empty()
//                            : validator.execute(path, method, null))
//                    .flatMap(details -> {
//                        return Mono.just(exchange
//                                .mutate()
//                                .request(request
//                                        .mutate()
//                                        .header("P6e-User-Permission", JsonUtil.toJson(details))
//                                        .build()
//                                ).build());
//                    })
//                    .flatMap(chain::filter)
//                    .switchIfEmpty(exceptionErrorResult(exchange));
            return null;
        }

        @Override
        public int getOrder() {
            return ORDER;
        }
    }

}
