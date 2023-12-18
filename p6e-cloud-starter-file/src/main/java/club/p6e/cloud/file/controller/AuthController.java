package club.p6e.cloud.file.controller;

import club.p6e.cloud.file.cache.AuthCache;
import club.p6e.cloud.file.cache.VoucherCache;
import club.p6e.coat.common.context.ResultContext;
import club.p6e.coat.common.controller.BaseWebFluxController;
import club.p6e.coat.common.error.AuthException;
import club.p6e.coat.common.utils.GeneratorUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

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
    public AuthController(AuthCache authCache, VoucherCache voucherCache) {
        this.authCache = authCache;
        this.voucherCache = voucherCache;
    }

    @RequestMapping("")
    public Mono<ResultContext> def(ServerWebExchange exchange) {
        final String token = BaseWebFluxController.getAccessToken(exchange.getRequest());
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
                    .map(ResultContext::build);
        }
    }

}
