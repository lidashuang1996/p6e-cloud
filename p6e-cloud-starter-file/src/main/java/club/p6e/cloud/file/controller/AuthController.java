package club.p6e.cloud.file.controller;

import club.p6e.cloud.file.cache.AuthCache;
import club.p6e.cloud.file.cache.VoucherCache;
import club.p6e.coat.file.error.AuthException;
import club.p6e.coat.file.handler.AspectHandlerFunction;
import club.p6e.coat.file.utils.GeneratorUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author lidashuang
 * @version 1.0
 */
@Component
@RestController
@RequestMapping("/auth")
@ConditionalOnMissingBean(
        value = AuthController.class,
        ignored = AuthController.class
)
public class AuthController {

    /**
     * 使用的请求头名称
     */
    private static final String AUTH_HEADER = "Authorization";
    private static final String AUTH_HEADER_TOKEN_TYPE = "Bearer";
    private static final String AUTH_HEADER_TOKEN_PREFIX = AUTH_HEADER_TOKEN_TYPE + " ";
    private static final String ACCESS_TOKEN_PARAM1 = "accessToken";
    private static final String ACCESS_TOKEN_PARAM2 = "access_token";
    private static final String ACCESS_TOKEN_PARAM3 = "access-token";
    private static final String COOKIE_ACCESS_TOKEN = "ACCESS_TOKEN";

    /**
     * 认证缓存对象
     */
    private final AuthCache authCache;

    /**
     * 凭证缓存对象
     */
    private final VoucherCache voucherCache;

    /**
     * 通过多个参数名称去获取 URL 路径上面的参数值
     *
     * @param params 参数名称
     * @return 读取的参数名称对应的值
     */
    public static String getParam(ServerHttpRequest request, String... params) {
        String value;
        for (String param : params) {
            value = request.getQueryParams().getFirst(param);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /**
     * 获取请求头部的信息
     *
     * @return 请求头部的信息
     */
    public static String getHeader(ServerHttpRequest request, String name) {
        if (name != null) {
            for (final String key : request.getHeaders().keySet()) {
                if (name.equalsIgnoreCase(key)) {
                    return request.getHeaders().getFirst(key);
                }
            }
        }
        return null;
    }

    /**
     * 获取请求头部存在的 TOKEN 信息
     *
     * @return 头部的 TOKEN 信息
     */
    public static String getHeaderToken(ServerHttpRequest request) {
        final String requestHeaderContent = getHeader(request, AUTH_HEADER);
        if (requestHeaderContent != null
                && requestHeaderContent.startsWith(AUTH_HEADER_TOKEN_PREFIX)) {
            return requestHeaderContent.substring(AUTH_HEADER_TOKEN_PREFIX.length());
        }
        return null;
    }

    /**
     * 获取 COOKIE 的信息
     *
     * @return COOKIE 的信息
     */
    public static List<HttpCookie> getCookie(ServerHttpRequest request, String name) {
        return request.getCookies().get(name);
    }

    /**
     * 获取 COOKIE 的 TOKEN 信息
     *
     * @return COOKIE 的 ACCESS TOKEN 信息
     */
    public static String getCookieAccessToken(ServerHttpRequest request) {
        final List<HttpCookie> cookies = getCookie(request, COOKIE_ACCESS_TOKEN);
        if (cookies != null && !cookies.isEmpty()) {
            return cookies.get(0).getValue();
        }
        return null;
    }

    /**
     * 获取 ACCESS TOKEN 内容
     *
     * @param request ServerHttpRequest 对象
     * @return ACCESS TOKEN 内容
     */
    public static String getAccessToken(ServerHttpRequest request) {
        String accessToken = getParam(request, ACCESS_TOKEN_PARAM1, ACCESS_TOKEN_PARAM2, ACCESS_TOKEN_PARAM3);
        if (accessToken == null) {
            accessToken = getHeaderToken(request);
        }
        if (accessToken == null) {
            accessToken = getCookieAccessToken(request);
        }
        return accessToken;
    }


    /**
     * 构造方法初始化
     *
     * @param authCache    认证缓存对象
     * @param voucherCache 凭证缓存对象
     */
    public AuthController(AuthCache authCache, VoucherCache voucherCache) {
        this.authCache = authCache;
        this.voucherCache = voucherCache;
    }

    @RequestMapping("")
    public Mono<AspectHandlerFunction.ResultContext> def(ServerWebExchange exchange) {
        final String token = getAccessToken(exchange.getRequest());
        if (token == null) {
            return Mono.error(new AuthException(
                    this.getClass(),
                    "fun def(ServerWebExchange exchange).",
                    "Request missing authentication information"
            ));
        } else {
            return authCache
                    .getAccessToken(token)
                    .flatMap(t -> {
                        final String voucher = GeneratorUtil.uuid() + GeneratorUtil.random();
                        return voucherCache.set(voucher, t.getUid()).map(s -> voucher);
                    })
                    .switchIfEmpty(Mono.error(new AuthException(
                            this.getClass(),
                            "fun def(ServerWebExchange exchange).",
                            "Request authentication information has expired"
                    )))
                    .map(AspectHandlerFunction.ResultContext::build);
        }
    }

}
