package club.p6e.cloud.file.controller;

import club.p6e.cloud.file.cache.AuthCache;
import club.p6e.cloud.file.cache.VoucherCache;
import club.p6e.coat.common.context.ResultContext;
import club.p6e.coat.common.controller.BaseWebFluxController;
import club.p6e.coat.common.error.AuthException;
import club.p6e.coat.common.utils.GeneratorUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author lidashuang
 * @version 1.0
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthCache authCache;
    private final VoucherCache voucherCache;

    public AuthController(AuthCache authCache, VoucherCache voucherCache) {
        this.authCache = authCache;
        this.voucherCache = voucherCache;
    }

    public Mono<ResultContext> def(ServerWebExchange exchange) {
        final String token = BaseWebFluxController.getAccessToken(exchange.getRequest());
        if (token == null) {
            return Mono.error(new AuthException(
                    this.getClass(), "fun def(ServerWebExchange exchange)."));
        } else {
            return authCache
                    .getAccessToken(token)
                    .flatMap(t -> {
                        final String voucher = GeneratorUtil.uuid() + GeneratorUtil.random();
                        return voucherCache.set(voucher, t.getUid()).map(s -> voucher);
                    })
                    .switchIfEmpty(Mono.error(new AuthException(
                            this.getClass(), "fun def(ServerWebExchange exchange).")))
                    .map(ResultContext::build);
        }
    }

}
