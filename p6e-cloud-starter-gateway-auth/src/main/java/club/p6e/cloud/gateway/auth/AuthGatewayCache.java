package club.p6e.cloud.gateway.auth;

import lombok.Data;
import lombok.experimental.Accessors;
import reactor.core.publisher.Mono;

import java.io.Serializable;

/**
 * Auth Gateway Cache
 *
 * @author lidashuang
 * @version 1.0
 */
public interface AuthGatewayCache {

    /**
     * Token Model
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
     * Delimiter
     */
    String DELIMITER = ":";

    /**
     * Expiration Time
     */
    long EXPIRATION_TIME = 3600;

    /**
     * User Cache Prefix
     */
    String USER_PREFIX = "AUTH:USER:";

    /**
     * Access Token Cache Prefix
     */
    String ACCESS_TOKEN_PREFIX = "AUTH:ACCESS_TOKEN:";

    /**
     * Refresh Token Cache Prefix
     */
    String REFRESH_TOKEN_PREFIX = "AUTH:REFRESH_TOKEN:";

    /**
     * User Access Token Cache Prefix
     */
    String USER_ACCESS_TOKEN_PREFIX = "AUTH:USER:ACCESS_TOKEN:";

    /**
     * User Refresh Token Cache Prefix
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
     * Refresh
     *
     * @param token Token object
     * @return Token object
     */
    Mono<Token> refresh(Token token);

    /**
     * Refresh
     *
     * @param uid User Id
     * @return User Content
     */
    Mono<String> refreshUser(String uid);

    /**
     * Get Refresh Token Content
     *
     * @param token Refresh Token
     * @return Refresh Token Content
     */
    Mono<Token> refreshToken(Token token);

    /**
     * Get Access Token Content
     *
     * @param token Access Token
     * @return Access Token Content
     */
    Mono<Token> getAccessToken(String token);

    /**
     * Get Access Token Expiration Time
     *
     * @param token Access Token
     * @return Access Token Expiration Time
     */
    Mono<Long> getAccessTokenExpire(String token);

}
