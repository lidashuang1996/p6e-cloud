package club.p6e.cloud.websocket.cache;

import lombok.Data;
import lombok.experimental.Accessors;
import reactor.core.publisher.Mono;

import java.io.Serializable;

/**
 * 认证缓存服务
 *
 * @author lidashuang
 * @version 1.0
 */
public interface AuthCache {

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

    }

    /**
     * 用户缓存前缀
     */
    String USER_PREFIX = "AUTH:USER:";

    /**
     * ACCESS TOKEN 缓存前缀
     */
    String ACCESS_TOKEN_PREFIX = "AUTH:ACCESS_TOKEN:";

    /**
     * 读取用户内容
     *
     * @param uid 用户
     * @return 读取用户内容
     */
    Mono<String> getUser(String uid);

    /**
     * 读取 ACCESS TOKEN 令牌内容
     *
     * @param token 令牌
     * @return ACCESS TOKEN 令牌内容
     */
    Mono<Token> getAccessToken(String token);

}
