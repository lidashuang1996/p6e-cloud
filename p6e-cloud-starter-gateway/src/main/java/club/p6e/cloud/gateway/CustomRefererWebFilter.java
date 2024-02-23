package club.p6e.cloud.gateway;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 自定义全局 REFERER 过滤器
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = CustomRefererWebFilter.class,
        ignored = CustomRefererWebFilter.class
)
public class CustomRefererWebFilter implements WebFilter, Ordered {

    /**
     * 执行顺序
     */
    protected static final int ORDER = -2600;

    /**
     * REFERER
     */
    protected static final String REFERER_HEADER = "Referer";

    /**
     * REFERER 通用内容
     */
    protected static final String REFERER_HEADER_GENERAL_CONTENT = "*";

    /**
     * 错误结果内容
     */
    protected static final String ERROR_RESULT_CONTENT =
            "{\"code\":403,\"message\":\"Forbidden\",\"data\":\"Referer Exception\"}";

    /**
     * 配置文件对象
     */
    protected final Properties properties;

    /**
     * 构造方法初始化
     *
     * @param properties 配置文件对象
     */
    public CustomRefererWebFilter(Properties properties) {
        this.properties = properties;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    @NonNull
    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        if (!properties.getReferer().isEnable()) {
            return chain.filter(exchange);
        }

        final ServerHttpRequest request = exchange.getRequest();
        final ServerHttpResponse response = exchange.getResponse();
        final List<String> refererList = request.getHeaders().get(REFERER_HEADER);

        if (refererList != null && !refererList.isEmpty()) {
            final String r = refererList.get(0);
            final String referer = r == null ? "" : r;
            for (final String item : properties.getReferer().getWhiteList()) {
                if (REFERER_HEADER_GENERAL_CONTENT.equals(item) || referer.startsWith(item)) {
                    return chain.filter(exchange);
                }
            }
        }

        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.writeWith(Mono.just(response.bufferFactory()
                .wrap(ERROR_RESULT_CONTENT.getBytes(StandardCharsets.UTF_8))));
    }
}
