package club.p6e.cloud.gateway.auth;

import club.p6e.coat.common.utils.JsonUtil;
import club.p6e.coat.common.utils.TransformationUtil;
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
import java.util.Map;
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

    public static long AUTH_EXPIRATION_TIME = 3600L;

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

    private String getProject(Map<String, Object> data) {
        if (data == null) {
            return null;
        } else {
            return TransformationUtil.objectToString(data.get("pid"));
        }
    }

    @Override
    public Mono<String> getUser(String uid, Map<String, Object> data) {
        final String pid = getProject(data);
        if (pid == null) {
            return Mono.empty();
        } else {
            return template.opsForValue().get("PROJECT:" + pid + ":" + USER_PREFIX + uid);
        }
    }

    @Override
    public Mono<Token> refresh(Token token, Map<String, Object> data) {
        final String pid = getProject(data);
        if (pid == null) {
            return Mono.empty();
        } else {
            return refreshUser(token.getUid(), data).flatMap(c -> refreshToken(token, data));
        }
    }

    @Override
    public Mono<String> refreshUser(String uid, Map<String, Object> data) {
        final String pid = getProject(data);
        if (pid == null) {
            return Mono.empty();
        } else {
            return template
                    .opsForValue()
                    .get("PROJECT:" + pid + ":" + USER_PREFIX + uid)
                    .flatMap(content -> template.opsForValue().set(
                            "PROJECT:" + pid + ":" + USER_PREFIX + uid, content, Duration.ofSeconds(AUTH_EXPIRATION_TIME)
                    ))
                    .flatMap(r -> template
                            .hasKey("PROJECT:" + pid + ":AUTH:USER:DATETIME:" + uid)
                            .flatMap(b -> {
                                if (b) {
                                    return template
                                            .opsForValue()
                                            .get("PROJECT:" + pid + ":AUTH:USER:DATETIME:" + uid)
                                            .flatMap(dateTime -> template.opsForValue().set(
                                                    "PROJECT:" + pid + ":AUTH:USER:DATETIME:" + uid, dateTime, Duration.ofSeconds(AUTH_EXPIRATION_TIME)
                                            )).map(rr -> uid);
                                } else {
                                    return Mono.just(uid);
                                }
                            })
                    );
        }
    }

    @Override
    public Mono<Token> refreshToken(Token token, Map<String, Object> data) {
        final String pid = getProject(data);
        if (pid == null) {
            return Mono.empty();
        } else {
            final String content = JsonUtil.toJson(token);
            if (content == null) {
                return Mono.empty();
            }
            final byte[] jcBytes = content.getBytes(StandardCharsets.UTF_8);
            return template.execute(connection ->
                    Flux.concat(
                            connection.stringCommands().set(
                                    ByteBuffer.wrap(("PROJECT:" + pid + ":" + ACCESS_TOKEN_PREFIX + token.getAccessToken()).getBytes(StandardCharsets.UTF_8)),
                                    ByteBuffer.wrap(jcBytes),
                                    Expiration.from(AUTH_EXPIRATION_TIME, TimeUnit.SECONDS),
                                    RedisStringCommands.SetOption.UPSERT
                            ),
                            connection.stringCommands().set(
                                    ByteBuffer.wrap(("PROJECT:" + pid + ":" + REFRESH_TOKEN_PREFIX + token.getRefreshToken()).getBytes(StandardCharsets.UTF_8)),
                                    ByteBuffer.wrap(jcBytes),
                                    Expiration.from(AUTH_EXPIRATION_TIME, TimeUnit.SECONDS),
                                    RedisStringCommands.SetOption.UPSERT
                            ),
                            connection.stringCommands().set(
                                    ByteBuffer.wrap(("PROJECT:" + pid + ":" + USER_ACCESS_TOKEN_PREFIX + token.getUid() + DELIMITER + token.getAccessToken()).getBytes(StandardCharsets.UTF_8)),
                                    ByteBuffer.wrap(jcBytes),
                                    Expiration.from(AUTH_EXPIRATION_TIME, TimeUnit.SECONDS),
                                    RedisStringCommands.SetOption.UPSERT
                            ),
                            connection.stringCommands().set(
                                    ByteBuffer.wrap(("PROJECT:" + pid + ":" + USER_REFRESH_TOKEN_PREFIX + token.getUid() + DELIMITER + token.getRefreshToken()).getBytes(StandardCharsets.UTF_8)),
                                    ByteBuffer.wrap(jcBytes),
                                    Expiration.from(AUTH_EXPIRATION_TIME, TimeUnit.SECONDS),
                                    RedisStringCommands.SetOption.UPSERT
                            )
                    )
            ).collectList().map(l -> token);
        }
    }

    @Override
    public Mono<Token> getAccessToken(String token, Map<String, Object> data) {
        final String pid = getProject(data);
        if (pid == null) {
            return Mono.empty();
        } else {
            return template
                    .opsForValue()
                    .get("PROJECT:" + pid + ":" + ACCESS_TOKEN_PREFIX + token)
                    .flatMap(content -> {
                        final Token result = JsonUtil.fromJson(content, Token.class);
                        return result == null ? Mono.empty() : Mono.just(result);
                    });
        }
    }

    @Override
    public Mono<Long> getAccessTokenExpire(String token, Map<String, Object> data) {
        final String pid = getProject(data);
        if (pid == null) {
            return Mono.empty();
        } else {
            return template
                    .getExpire("PROJECT:" + pid + ":" + ACCESS_TOKEN_PREFIX + token)
                    .map(Duration::getSeconds);
        }
    }

}
