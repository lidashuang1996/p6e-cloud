package club.p6e.cloud.gateway.auth;

import club.p6e.coat.common.controller.BaseWebFluxController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Custom Auth Web Filter
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = AuthGlobalFilter.class,
        ignored = AuthGlobalFilter.class
)
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    /**
     * Order
     */
    private static final int ORDER = Integer.MAX_VALUE - 3000;

    /**
     * P6e User Auth Header Name
     */
    @SuppressWarnings("ALL")
    private static final String USER_AUTH_HEADER = "P6e-User-Auth";

    /**
     * P6e User Info Header Name
     */
    @SuppressWarnings("ALL")
    protected static final String USER_INFO_HEADER = "P6e-User-Info";

    /**
     * P6e User Permission Header Name
     */
    @SuppressWarnings("ALL")
    protected static final String USER_INFO_PERMISSION_HEADER = "P6e-User-Permission";

    @Override
    public int getOrder() {
        return ORDER;
    }

    /**
     * Date Time Formatter Object
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Exception Error Result
     *
     * @param exchange ServerWebExchange object
     * @return Mono<Void> Void object
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
        final ServerHttpRequest request = exchange.getRequest();
        final String userAuth = BaseWebFluxController.getHeader(request, USER_AUTH_HEADER);
        // 1 >> BASE64 >> MQ==  // [1] [MQ==]
        if ("1".equals(userAuth) || "MQ==".equals(userAuth)) {
            final String userInfo = BaseWebFluxController.getHeader(request, USER_INFO_HEADER);
            final String userPermission = BaseWebFluxController.getHeader(request, USER_INFO_PERMISSION_HEADER);
            if ((userInfo == null || userInfo.isEmpty()) && (userPermission == null || userPermission.isEmpty())) {
                return exceptionErrorResult(exchange);
            }
        }
        return chain.filter(exchange);
    }

}
