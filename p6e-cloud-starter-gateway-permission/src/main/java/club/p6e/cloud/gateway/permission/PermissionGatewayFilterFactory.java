package club.p6e.cloud.gateway.permission;

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

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 权限网关过滤器工厂
 *
 * @author lidashuang
 * @version 1.0
 */
public class PermissionGatewayFilterFactory
        extends AbstractGatewayFilterFactory<Object> {

    /**
     * 顺序（越小越先被执行）
     */
    private static final int ORDER = -900;

    /**
     * 权限服务对象
     */
    private final PermissionGatewayService service;

    /**
     * 构造方法初始化
     *
     * @param service 权限服务对象
     */
    public PermissionGatewayFilterFactory(PermissionGatewayService service) {
        this.service = service;
    }

    @Override
    public GatewayFilter apply(Object config) {
        return new CustomGatewayFilter(service);
    }

    /**
     * 自定义网关过滤器
     *
     * @param service 权限服务
     */
    private record CustomGatewayFilter(PermissionGatewayService service) implements GatewayFilter, Ordered {

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
                    + request.getPath() + "\",\"message\":\"NO_PERMISSION\",\"requestId\":\"" + request.getId() + "\",\"code\":403}";
            response.setStatusCode(HttpStatus.FORBIDDEN);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(result.getBytes(StandardCharsets.UTF_8))));
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            final AtomicReference<ServerWebExchange> atomicReference = new AtomicReference<>(exchange);
            return service
                    .execute(exchange)
                    .map(e -> {
                        atomicReference.set(e);
                        return true;
                    })
                    .switchIfEmpty(Mono.just(false))
                    .flatMap(r -> r ? chain.filter(atomicReference.get()) : exceptionErrorResult(exchange));
        }

        @Override
        public int getOrder() {
            return ORDER;
        }
    }

}
