package club.p6e.cloud.websocket.auth;

import club.p6e.cloud.websocket.UserModel;
import club.p6e.cloud.websocket.cache.AuthCache;
import club.p6e.cloud.websocket.cache.VoucherCache;
import club.p6e.coat.common.controller.BaseWebFluxController;
import club.p6e.coat.common.error.AuthException;
import club.p6e.coat.common.utils.GeneratorUtil;
import club.p6e.coat.common.utils.JsonUtil;
import club.p6e.coat.websocket.User;
import club.p6e.coat.websocket.auth.AuthWebFluxService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 认证重写
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnClass(name = "org.springframework.web.reactive.package-info")
public class CacheAuthWebFluxServiceImpl implements AuthWebFluxService {

    /**
     * 认证缓存对象
     */
    private final AuthCache authCache;

    /**
     * 凭证缓存对象
     */
    private final VoucherCache voucherCache;

    /**
     * 构造方法初始化
     *
     * @param authCache    认证缓存对象
     * @param voucherCache 凭证缓存对象
     */
    public CacheAuthWebFluxServiceImpl(AuthCache authCache, VoucherCache voucherCache) {
        this.authCache = authCache;
        this.voucherCache = voucherCache;
    }

    @Override
    public Mono<String> award(ServerWebExchange exchange) {
        String token = BaseWebFluxController.getAccessToken(exchange.getRequest());
        if (token == null) {
            throw new AuthException(this.getClass(),
                    "fun award(HttpServletRequest request).",
                    "request does not authentication information."
            );
        }
        return authCache
                .getAccessToken(token)
                .flatMap(t -> authCache.getUser(t.getUid()))
                .switchIfEmpty(Mono.error(new AuthException(
                        this.getClass(),
                        "fun award(HttpServletRequest request).",
                        "request for expired authentication information."
                )))
                .flatMap(content -> voucherCache.set(GeneratorUtil.uuid() + GeneratorUtil.random(), content));
    }

    @Override
    public Mono<User> validate(String voucher) {
        return voucher == null ? Mono.empty() : voucherCache.get(voucher)
                .flatMap(content -> {
                    final UserModel model = JsonUtil.fromJson(content, UserModel.class);
                    if (model == null) {
                        return voucherCache.del(voucher).flatMap(r -> Mono.empty());
                    } else {
                        return voucherCache.del(voucher).map(r -> model);
                    }
                });
    }
}
