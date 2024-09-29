package club.p6e.cloud.gateway;

import org.reactivestreams.Publisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

/**
 * Custom Response Header Only WebFilter
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = CustomResponseHeaderOnlyWebFilter.class,
        ignored = CustomResponseHeaderOnlyWebFilter.class
)
public class CustomResponseHeaderOnlyWebFilter implements WebFilter, Ordered {

    /**
     * Order
     */
    private static final int ORDER = Integer.MAX_VALUE - 2000;

    /**
     * Only Response Header
     */
    private static final String[] HEADER_FILTERED = new String[]{
            "Content-Type",
            "Access-Control",
    };

    /**
     * Properties object
     */
    private final Properties properties;

    /**
     * Constructor initializers
     *
     * @param properties Properties object
     */
    public CustomResponseHeaderOnlyWebFilter(Properties properties) {
        this.properties = properties;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    @NonNull
    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        final ServerHttpResponse response = exchange.getResponse();
        final ServerHttpResponseDecorator responseDecorator = new ServerHttpResponseDecorator(response) {
            @NonNull
            @Override
            public Mono<Void> writeWith(@NonNull Publisher<? extends DataBuffer> body) {
                final HttpHeaders httpHeaders = response.getHeaders();
                final Set<String> httpHeaderNames = httpHeaders.keySet();
                for (final String httpHeaderName : httpHeaderNames) {
                    final List<String> httpHeaderValue = httpHeaders.get(httpHeaderName);
                    if (httpHeaderValue != null && !httpHeaderValue.isEmpty()) {
                        for (final String header : HEADER_FILTERED) {
                            if (httpHeaderName.equalsIgnoreCase(header)) {
                                httpHeaders.set(httpHeaderName, httpHeaderValue.get(0));
                            }
                        }
                        for (final String header : properties.getResponseHeaderOnly()) {
                            if (httpHeaderName.equalsIgnoreCase(header)) {
                                httpHeaders.set(httpHeaderName, httpHeaderValue.get(0));
                            }
                        }
                    }
                }
                return super.writeWith(body);
            }
        };
        return chain.filter(exchange.mutate().response(responseDecorator).build());
    }
}
