package club.p6e.cloud.gateway;

import org.reactivestreams.Publisher;
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
 * 自定义返回头清除过滤器
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
public class CustomResponseHeaderClearWebFilter implements WebFilter, Ordered {

    /**
     * 执行顺序
     */
    private static final int ORDER = -2900;

    /**
     * 需要唯一的数据头
     */
    private static final String[] HEADER_FILTERED = new String[]{
            "Content-Type",
            "Access-Control",
    };

    /**
     * 配置文件对象
     */
    private final Properties properties;

    /**
     * 构造方法初始化
     *
     * @param properties 配置文件对象
     */
    public CustomResponseHeaderClearWebFilter(Properties properties) {
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
                for (String httpHeaderName : httpHeaderNames) {
                    final List<String> httpHeaderValue = httpHeaders.get(httpHeaderName);
                    if (httpHeaderValue != null && !httpHeaderValue.isEmpty()) {
                        for (final String header : HEADER_FILTERED) {
                            if (httpHeaderName.startsWith(header)) {
                                httpHeaders.set(httpHeaderName, httpHeaderValue.get(0));
                            }
                        }
                        for (final String header : properties.getResponseHeaderOnly()) {
                            if (httpHeaderName.startsWith(header)) {
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
