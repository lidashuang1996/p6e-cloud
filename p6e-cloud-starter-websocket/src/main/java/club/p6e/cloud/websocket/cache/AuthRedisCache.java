package club.p6e.cloud.websocket.cache;

import club.p6e.coat.common.utils.JsonUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author lidashuang
 * @version 1.0
 */
@Component
public class AuthRedisCache implements AuthCache {

    /**
     * 缓存对象
     */
    private final StringRedisTemplate template;

    /**
     * 构造方法初始化
     *
     * @param template 缓存对象
     */
    public AuthRedisCache(StringRedisTemplate template) {
        this.template = template;
    }

    @Override
    public String getUser(String uid) {
        try {
            return template.opsForValue().get(USER_PREFIX + uid);
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    @Override
    public Token getAccessToken(String token) {
        try {
            final String content = template.opsForValue().get(ACCESS_TOKEN_PREFIX + token);
            if (content != null) {
                return JsonUtil.fromJson(content, Token.class);
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

}
