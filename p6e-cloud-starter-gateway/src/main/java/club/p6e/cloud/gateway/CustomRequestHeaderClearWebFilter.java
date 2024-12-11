package club.p6e.cloud.gateway;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Custom Request Header Clear Web Filter
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = CustomRequestHeaderClearWebFilter.class,
        ignored = CustomRequestHeaderClearWebFilter.class
)
public class CustomRequestHeaderClearWebFilter implements WebFilter, Ordered {

    /**
     * Order
     */
    protected static final int ORDER = Integer.MIN_VALUE + 2000;

    /**
     * Reset default data content
     */
    protected static final String HEADER_CONTENT_NULL = "";

    /**
     * Request headers that need to be reset
     */
    protected static final String[] HEADER_FILTERED = new String[]{
            "P6e-Voucher",
            "P6e-Language",
            "P6e-User-Info",
            "P6e-User-Auth",
            "P6e-User-Permission",
            "P6e-User-Project",
            "P6e-User-Organization"
    };

    /**
     * Properties object
     */
    protected final Properties properties;

    /**
     * Constructor initializers
     *
     * @param properties Properties object
     */
    public CustomRequestHeaderClearWebFilter(Properties properties) {
        this.properties = properties;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    @NonNull
    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        final ServerHttpRequest request = exchange.getRequest();
        final ServerHttpRequest.Builder requestBuilder = request.mutate();
        // delete http headers
        // request header is used internally for calling
        // Prohibit sending requests that carry this request header to downstream services
        for (final String header : HEADER_FILTERED) {
            requestBuilder.header(header, HEADER_CONTENT_NULL);
        }
        for (final String header : properties.getRequestHeaderClear()) {
            requestBuilder.header(header, HEADER_CONTENT_NULL);
        }
        return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
    }

}
