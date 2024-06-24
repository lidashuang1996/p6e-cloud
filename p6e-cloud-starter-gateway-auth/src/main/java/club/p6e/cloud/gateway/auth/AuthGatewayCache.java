package club.p6e.cloud.gateway.auth;

import lombok.Data;
import lombok.experimental.Accessors;
import reactor.core.publisher.Mono;

import java.io.Serializable;

/**
 * 网关认证缓存接口
 *
 * @author lidashuang
 * @version 1.0
 */
public interface AuthGatewayCache {

    /**
     * 令牌模型
     */
    @Data
    @Accessors(chain = true)
    class Token implements Serializable {

        /**
         * UID
         */
        private String uid;

        /**
         * ACCESS TOKEN
         */
        private String accessToken;

        /**
         * REFRESH TOKEN
         */
        private String refreshToken;

    }

    /**
     * 分割符号
     */
    String DELIMITER = ":";

    /**
     * 过期的时间
     */
    long EXPIRATION_TIME = 3600 * 3L;

    /**
     * 用户缓存前缀
     */
    String USER_PREFIX = "AUTH:USER:";

    /**
     * ACCESS TOKEN 缓存前缀
     */
    String ACCESS_TOKEN_PREFIX = "AUTH:ACCESS_TOKEN:";

    /**
     * REFRESH TOKEN 缓存前缀
     */
    String REFRESH_TOKEN_PREFIX = "AUTH:REFRESH_TOKEN:";

    /**
     * ACCESS TOKEN 缓存前缀
     */
    String USER_ACCESS_TOKEN_PREFIX = "AUTH:USER:ACCESS_TOKEN:";

    /**
     * REFRESH TOKEN 缓存前缀
     */
    String USER_REFRESH_TOKEN_PREFIX = "AUTH:USER:REFRESH_TOKEN:";

    /**
     * 读取用户内容
     *
     * @param uid 用户
     * @return 读取用户内容
     */
    Mono<String> getUser(String uid);

    /**
     * 刷新内容
     *
     * @param token 令牌
     * @return 令牌内容
     */
    Mono<Token> refresh(Token token);

    /**
     * 刷新用户内容
     *
     * @param uid 用户
     * @return 用户内容
     */
    Mono<String> refreshUser(String uid);

    /**
     * 刷新令牌内容
     *
     * @param token 令牌
     * @return 令牌内容
     */
    Mono<Token> refreshToken(Token token);

    /**
     * 读取 ACCESS TOKEN 令牌内容
     *
     * @param token 令牌
     * @return ACCESS TOKEN 令牌内容
     */
    Mono<Token> getAccessToken(String token);

    /**
     * 读取 ACCESS TOKEN 令牌过期时间
     *
     * @param token 令牌
     * @return ACCESS TOKEN 令牌过期时间
     */
    Mono<Long> getAccessTokenExpire(String token);

}
