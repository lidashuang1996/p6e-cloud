package club.p6e.cloud.websocket;

import club.p6e.cloud.websocket.cache.AuthCache;
import club.p6e.cloud.websocket.cache.VoucherCache;
import club.p6e.coat.common.controller.BaseWebController;
import club.p6e.coat.common.error.AuthException;
import club.p6e.coat.common.utils.GeneratorUtil;
import club.p6e.coat.common.utils.JsonUtil;
import club.p6e.coat.websocket.Auth;
import club.p6e.coat.websocket.DefaultAuth;
import club.p6e.coat.websocket.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * 认证重写
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
public class CustomAuth implements Auth {

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
    public CustomAuth(AuthCache authCache, VoucherCache voucherCache) {
        this.authCache = authCache;
        this.voucherCache = voucherCache;
    }

    @Override
    public String award(HttpServletRequest request) {
        String token = BaseWebController.getHeaderToken();
        if (token == null) {
            token = BaseWebController.getAccessToken();
        }
        if (token == null) {
            throw new AuthException(this.getClass(),
                    "fun award(HttpServletRequest request).");
        }
        final AuthCache.Token authToken = authCache.getAccessToken(token);
        if (authToken == null) {
            throw new AuthException(this.getClass(),
                    "fun award(HttpServletRequest request).");
        }
        final String content = authCache.getUser(authToken.getUid());
        if (content == null) {
            throw new AuthException(this.getClass(),
                    "fun award(HttpServletRequest request).");
        }
        final String voucher = GeneratorUtil.uuid() + GeneratorUtil.random();
        voucherCache.set(voucher, content);
        return voucher;
    }

    @Override
    public User validate(String uri) {
        final String voucher = DefaultAuth.getVoucher(uri);
        if (voucher != null) {
            final String content = voucherCache.get(voucher);
            if (content != null) {
                try {
                    return JsonUtil.fromJson(content, CustomUser.class);
                } catch (Exception e) {
                    // ignore
                } finally {
                    voucherCache.del(voucher);
                }
            }
        }
        return null;
    }
}
