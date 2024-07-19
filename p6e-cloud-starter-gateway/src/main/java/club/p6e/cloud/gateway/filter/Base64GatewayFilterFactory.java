package club.p6e.cloud.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author lidashuang
 * @version 1.0
 */
@Component
public class Base64GatewayFilterFactory
        extends AbstractGatewayFilterFactory<Object> {

    /**
     * 顺序
     */
    private static final int ORDER = 10000;

    @Override
    public GatewayFilter apply(Object config) {
        return new CustomGatewayFilter();
    }

    /**
     * 网关过滤器
     */
    @SuppressWarnings("ALL")
    public static class CustomGatewayFilter implements GatewayFilter, Ordered {

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            final ServerHttpRequest request = exchange.getRequest();
            final HttpHeaders httpHeaders = request.getHeaders();
            final ServerHttpRequest.Builder builder = request.mutate();
            for (final String name : httpHeaders.keySet()) {
                if (name != null && name.toLowerCase().startsWith("p6e-")) {
                    System.out.println("name >> " + name);
                    System.out.println("value >> " + httpHeaders.getFirst(name));
                    System.out.println("base64 >> " + Base64.getEncoder().encodeToString(
                            httpHeaders.getFirst(name.toLowerCase()).getBytes(StandardCharsets.UTF_8)));
                    builder.header(name, Base64.getEncoder().encodeToString(
                            httpHeaders.getFirst(name.toLowerCase()).getBytes(StandardCharsets.UTF_8)));
                }
            }
            return chain.filter(exchange.mutate().request(builder.build()).build());
        }

        @Override
        public int getOrder() {
            return ORDER;
        }

    }

}
