package club.p6e.cloud.gateway.auth;

import club.p6e.coat.common.utils.JsonUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Auth Gateway Redis Cache
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
     * ReactiveStringRedisTemplate object
     */
    private final ReactiveStringRedisTemplate template;

    /**
     * Constructor initializers
     *
     * @param template ReactiveRedisConnectionFactory object
     */
    public AuthGatewayRedisCache(ReactiveStringRedisTemplate template) {
        this.template = template;
    }

    @Override
    public Mono<String> getUser(String uid) {
        return template.opsForValue().get(USER_PREFIX + uid);
    }

    @Override
    public Mono<Token> refresh(Token token) {
        return refreshUser(token.getUid()).flatMap(c -> refreshToken(token));
    }

    @Override
    public Mono<String> refreshUser(String uid) {
        return template
                .opsForValue()
                .get(USER_PREFIX + uid)
                .flatMap(content -> template.opsForValue().set(
                        USER_PREFIX + uid, content, Duration.ofSeconds(EXPIRATION_TIME)
                ).map(b -> content));
    }

    @Override
    public Mono<Token> refreshToken(Token token) {
        final String data = JsonUtil.toJson(token);
        if (data == null) {
            return Mono.empty();
        }
        final byte[] jcBytes = data.getBytes(StandardCharsets.UTF_8);
        return template.execute(connection ->
                Flux.concat(
                        connection.stringCommands().set(
                                ByteBuffer.wrap((ACCESS_TOKEN_PREFIX + token.getAccessToken()).getBytes(StandardCharsets.UTF_8)),
                                ByteBuffer.wrap(jcBytes),
                                Expiration.from(EXPIRATION_TIME, TimeUnit.SECONDS),
                                RedisStringCommands.SetOption.UPSERT
                        ),
                        connection.stringCommands().set(
                                ByteBuffer.wrap((REFRESH_TOKEN_PREFIX + token.getRefreshToken()).getBytes(StandardCharsets.UTF_8)),
                                ByteBuffer.wrap(jcBytes),
                                Expiration.from(EXPIRATION_TIME, TimeUnit.SECONDS),
                                RedisStringCommands.SetOption.UPSERT
                        ),
                        connection.stringCommands().set(
                                ByteBuffer.wrap((USER_ACCESS_TOKEN_PREFIX + token.getUid() + DELIMITER + token.getAccessToken()).getBytes(StandardCharsets.UTF_8)),
                                ByteBuffer.wrap(jcBytes),
                                Expiration.from(EXPIRATION_TIME, TimeUnit.SECONDS),
                                RedisStringCommands.SetOption.UPSERT
                        ),
                        connection.stringCommands().set(
                                ByteBuffer.wrap((USER_REFRESH_TOKEN_PREFIX + token.getUid() + DELIMITER + token.getRefreshToken()).getBytes(StandardCharsets.UTF_8)),
                                ByteBuffer.wrap(jcBytes),
                                Expiration.from(EXPIRATION_TIME, TimeUnit.SECONDS),
                                RedisStringCommands.SetOption.UPSERT
                        )
                )
        ).collectList().map(l -> token);
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

    @Override
    public Mono<Long> getAccessTokenExpire(String token) {
        return template
                .getExpire(ACCESS_TOKEN_PREFIX + token)
                .map(Duration::getSeconds);
    }

}
