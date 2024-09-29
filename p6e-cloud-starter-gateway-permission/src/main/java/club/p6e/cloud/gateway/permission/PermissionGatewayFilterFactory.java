package club.p6e.cloud.gateway.permission;

import club.p6e.coat.common.controller.BaseWebFluxController;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
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
 * Permission Gateway Filter Factory
 *
 * @author lidashuang
 * @version 1.0
 */
public class PermissionGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    /**
     * PermissionGatewayService object
     */
    private final PermissionGatewayService service;

    /**
     * Constructor initializers
     *
     * @param service PermissionGatewayService object
     */
    public PermissionGatewayFilterFactory(PermissionGatewayService service) {
        this.service = service;
    }

    @Override
    public GatewayFilter apply(Object config) {
        return new CustomGatewayFilter(service);
    }

    /**
     * Custom Gateway Filter
     *
     * @param service PermissionGatewayService object
     */
    private record CustomGatewayFilter(PermissionGatewayService service) implements GatewayFilter {

        /**
         * P6e User Info Header Name
         */
        @SuppressWarnings("ALL")
        protected static final String USER_INFO_HEADER = "P6e-User-Info";

        /**
         * Date Time Formatter object
         */
        private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            final AtomicReference<ServerWebExchange> atomicReference = new AtomicReference<>(exchange);
            return service
                    .execute(atomicReference.get())
                    .map(e -> {
                        atomicReference.set(e);
                        return true;
                    })
                    .switchIfEmpty(Mono.just(false))
                    .flatMap(r -> r ? chain.filter(atomicReference.get()) : exceptionErrorResult(exchange));
        }

        /**
         * Exception Error Result
         *
         * @param exchange ServerWebExchange object
         * @return Mono<Void> Void object
         */
        private static Mono<Void> exceptionErrorResult(ServerWebExchange exchange) {
            final ServerHttpRequest request = exchange.getRequest();
            final ServerHttpResponse response = exchange.getResponse();
            final String user = BaseWebFluxController.getHeader(request, USER_INFO_HEADER);
            final String result1 = "{\"timestamp\":\"" + DATE_TIME_FORMATTER.format(LocalDateTime.now()) + "\",\"path\":\""
                    + request.getPath() + "\",\"message\":\"NO_AUTH\",\"requestId\":\"" + request.getId() + "\",\"code\":401}";
            final String result2 = "{\"timestamp\":\"" + DATE_TIME_FORMATTER.format(LocalDateTime.now()) + "\",\"path\":\""
                    + request.getPath() + "\",\"message\":\"NO_PERMISSION\",\"requestId\":\"" + request.getId() + "\",\"code\":403}";
            response.setStatusCode(HttpStatus.FORBIDDEN);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(
                    ((user == null || user.isEmpty()) ? result1 : result2).getBytes(StandardCharsets.UTF_8)
            )));
        }

    }

}
