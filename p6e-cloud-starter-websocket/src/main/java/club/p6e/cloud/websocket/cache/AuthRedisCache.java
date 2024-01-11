package club.p6e.cloud.websocket.cache;

import club.p6e.coat.common.utils.JsonUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnClass(name = "org.springframework.web.reactive.package-info")
public class AuthRedisCache implements AuthCache {

    /**
     * 缓存对象
     */
    private final ReactiveStringRedisTemplate template;

    /**
     * 构造方法初始化
     *
     * @param template 缓存对象
     */
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
                    final Token t = JsonUtil.fromJson(content, Token.class);
                    return t == null ? Mono.empty() : Mono.just(t);
                });
    }

}
