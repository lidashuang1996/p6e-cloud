package club.p6e.cloud.file.cache;

import club.p6e.coat.common.utils.JsonUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = AuthCache.class,
        ignored = AuthRedisCache.class
)
public class AuthRedisCache implements AuthCache {

    private final ReactiveStringRedisTemplate template;

    public AuthRedisCache(ReactiveStringRedisTemplate template) {
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
