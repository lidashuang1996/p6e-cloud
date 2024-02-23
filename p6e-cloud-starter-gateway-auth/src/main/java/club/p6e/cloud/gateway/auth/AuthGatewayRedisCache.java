package club.p6e.cloud.gateway.auth;

import club.p6e.coat.common.utils.JsonUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * 网关认证缓存实现类
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = AuthGatewayCache.class,
        ignored = AuthGatewayRedisCache.class
)
public class AuthGatewayRedisCache implements AuthGatewayCache {

    /**
     * ReactiveStringRedisTemplate 对象
     */
    private final ReactiveStringRedisTemplate template;

    /**
     * 构造方法初始化
     *
     * @param template ReactiveStringRedisTemplate 对象
     */
    public AuthGatewayRedisCache(ReactiveStringRedisTemplate template) {
        this.template = template;
    }

    @Override
    public Mono<String> getUser(String uid) {
        return template.opsForValue().get(USER_PREFIX + uid);
    }

    @Override
    public Mono<Token> getAccessToken(String token) {
        return template
                .opsForValue()
                .get(ACCESS_TOKEN_PREFIX + token)
                .flatMap(content -> {
                    final Token result = JsonUtil.fromJson(content, Token.class);
                    return result == null ? Mono.empty() : Mono.just(result);
                });
    }
}
