package club.p6e.cloud.gateway.auth;

import club.p6e.coat.common.controller.BaseWebFluxController;
import club.p6e.coat.common.utils.JsonUtil;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 网关认证过滤器工厂类
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
public class AuthGatewayFilterFactory
        extends AbstractGatewayFilterFactory<Object> {

    /**
     * 顺序（越小越先被执行）
     */
    private static final int ORDER = -1000;

    /**
     * 认证服务
     */
    private final AuthGatewayService service;

    /**
     * 构造方法初始化
     *
     * @param service 认证服务
     */
    public AuthGatewayFilterFactory(AuthGatewayService service) {
        this.service = service;
    }

    @Override
    public GatewayFilter apply(Object config) {
        return new CustomGatewayFilter(service);
    }

    /**
     * 自定义网关过滤器
     *
     * @param service 认证服务
     */
    private record CustomGatewayFilter(AuthGatewayService service) implements GatewayFilter, Ordered {

        /**
         * 用户的信息头
         */
        @SuppressWarnings("ALL")
        private static final String USER_INFO_HEADER = "P6e-User-Info";

        /**
         * 不认证的请求参数
         */
        private static final String NO_AUTH_REQUEST_PARAM1 = "auth";

        /**
         * 不认证的请求参数
         */
        private static final String NO_AUTH_REQUEST_PARAM2 = "auth";

        /**
         * 格式化时间对象
         */
        private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        /**
         * 认证异常结果统一返回内容
         *
         * @param exchange 参数 ServerWebExchange 对象
         * @return Mono<Void> 请求结果直接返回
         */
        private static Mono<Void> exceptionErrorResult(ServerWebExchange exchange) {
            final ServerHttpRequest request = exchange.getRequest();
            final ServerHttpResponse response = exchange.getResponse();
            final String result = "{\"timestamp\":\"" + DATE_TIME_FORMATTER.format(LocalDateTime.now()) + "\",\"path\":\""
                    + request.getPath() + "\",\"message\":\"NO_AUTH\",\"requestId\":\"" + request.getId() + "\",\"code\":401}";
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(result.getBytes(StandardCharsets.UTF_8))));
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            final String ap = BaseWebFluxController.getParam(
                    exchange.getRequest(), NO_AUTH_REQUEST_PARAM1, NO_AUTH_REQUEST_PARAM2);
            final boolean ab = ap != null && ap.equals("0");
            final AtomicReference<ServerWebExchange> atomicReference = new AtomicReference<>(exchange);
            return service
                    .execute(exchange)
                    .map(e -> {
                        atomicReference.set(e);
                        return true;
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        if (ab) {
                            atomicReference.set(exchange.mutate().request(
                                    exchange.getRequest().mutate().header(USER_INFO_HEADER, initUserInfo()).build()
                            ).build());
                        }
                        return Mono.just(ab);
                    }))
                    .flatMap(r -> r ? chain.filter(atomicReference.get()) : exceptionErrorResult(exchange));
        }

        @Override
        public int getOrder() {
            return ORDER;
        }

        private static String initUserInfo() {
            return JsonUtil.toJson(new HashMap<>() {{
                put("id", 0);
                put("status", 1);
                put("enabled", 1);
                put("internal", 0);
                put("administrator", 0);
                put("name", "sys_anonymous_name");
                put("nickname", "sys_anonymous_nickname");
                put("account", "sys_anonymous_account");
                put("organizationalProject", new HashMap<>());
                put("permission", new HashMap<>() {{
                    put("group", "10000");
                }});
            }});
        }
    }

}
