package club.p6e.cloud.gateway;

import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
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
import java.util.Arrays;

/**
 * 自定义跨域过滤器
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
public class CustomCrossDomainWebFilter implements WebFilter, Ordered {

    /**
     * 执行顺序
     */
    private static final int ORDER = -2700;

    /**
     * 跨域配置 ACCESS_CONTROL_MAX_AGE
     */
    private static final long ACCESS_CONTROL_MAX_AGE = 3600L;

    /**
     * 跨域配置 ACCESS_CONTROL_ALLOW_ORIGIN
     */
    private static final boolean ACCESS_CONTROL_ALLOW_CREDENTIALS = true;

    /**
     * 跨域配置 ACCESS_CONTROL_ALLOW_HEADERS
     */
    private static final String[] ACCESS_CONTROL_ALLOW_HEADERS = new String[]{
            "Accept",
            "Host",
            "Origin",
            "Referer",
            "User-Agent",
            "Content-Type",
            "Authorization"
    };

    /**
     * 跨域配置 ACCESS_CONTROL_ALLOW_METHODS
     */
    private static final HttpMethod[] ACCESS_CONTROL_ALLOW_METHODS = new HttpMethod[]{
            HttpMethod.GET,
            HttpMethod.POST,
            HttpMethod.PUT,
            HttpMethod.DELETE,
            HttpMethod.OPTIONS,
    };

    /**
     * 错误结果内容
     */
    private static final String ERROR_RESULT_CONTENT =
            "{\"code\":403,\"message\":\"Forbidden\",\"data\":\"Forbidden\"}";

    /**
     * 配置文件对象
     */
    private final Properties properties;

    /**
     * 构造方法初始化
     *
     * @param properties 配置文件对象
     */
    public CustomCrossDomainWebFilter(Properties properties) {
        this.properties = properties;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    @NonNull
    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        if (!properties.getCrossDomain().isEnabled()) {
            return chain.filter(exchange);
        }

        final ServerHttpRequest request = exchange.getRequest();
        final ServerHttpResponse response = exchange.getResponse();

        final String origin = request.getHeaders().getOrigin();
        if (origin == null) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return response.writeWith(Mono.just(response.bufferFactory()
                    .wrap(ERROR_RESULT_CONTENT.getBytes(StandardCharsets.UTF_8))));
        } else {
            response.getHeaders().setAccessControlAllowOrigin(origin);
        }

        response.getHeaders().setAccessControlMaxAge(ACCESS_CONTROL_MAX_AGE);
        response.getHeaders().setAccessControlAllowCredentials(ACCESS_CONTROL_ALLOW_CREDENTIALS);
        response.getHeaders().setAccessControlAllowHeaders(Arrays.asList(ACCESS_CONTROL_ALLOW_HEADERS));
        response.getHeaders().setAccessControlAllowMethods(Arrays.asList(ACCESS_CONTROL_ALLOW_METHODS));

        if (HttpMethod.OPTIONS.matches(request.getMethod().name().toUpperCase())) {
            response.setStatusCode(HttpStatus.NO_CONTENT);
            return Mono.empty();
        } else {
            return chain.filter(exchange);
        }
    }
}

