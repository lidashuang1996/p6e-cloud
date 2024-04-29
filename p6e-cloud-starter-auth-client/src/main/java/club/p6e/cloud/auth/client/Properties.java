package club.p6e.cloud.auth.client;

import club.p6e.coat.common.utils.PropertiesUtil;
import club.p6e.coat.common.utils.TransformationUtil;
import club.p6e.coat.common.utils.YamlUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;

/**
 * @author lidashuang
 * @version 1.0
 */
@Data
@Primary
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Component(value = "club.p6e.cloud.auth.client.Properties")
@ConfigurationProperties(prefix = "p6e.cloud.auth.client")
public class Properties extends club.p6e.coat.auth.client.Properties implements Serializable {

    private static void initBase(
            Properties properties,
            String authorizeUrl,
            String authorizeTokenUrl,
            String authorizeAppId,
            String authorizeAppSecret,
            String authorizeAppRedirectUri,
            Map<String, Object> authorizeAppExtend,
            String jwtAccessTokenSecret,
            String jwtRefreshTokenSecret
    ) {
        if (authorizeUrl != null) {
            properties.setAuthorizeUrl(authorizeUrl);
        }
        if (authorizeTokenUrl != null) {
            properties.setAuthorizeTokenUrl(authorizeTokenUrl);
        }
        if (authorizeAppId != null) {
            properties.setAuthorizeAppId(authorizeAppId);
        }
        if (authorizeAppSecret != null) {
            properties.setAuthorizeAppSecret(authorizeAppSecret);
        }
        if (authorizeAppRedirectUri != null) {
            properties.setAuthorizeAppRedirectUri(authorizeAppRedirectUri);
        }
        if (authorizeAppExtend != null) {
            properties.getAuthorizeAppExtend().clear();
            for (final String key : authorizeAppExtend.keySet()) {
                if (key != null) {
                    final Object value = authorizeAppExtend.get(key);
                    if (value != null) {
                        properties.getAuthorizeAppExtend().put(key, String.valueOf(value));
                    }
                }
            }
        }
        if (jwtAccessTokenSecret != null) {
            properties.setJwtAccessTokenSecret(jwtAccessTokenSecret);
        }
        if (jwtRefreshTokenSecret != null) {
            properties.setJwtRefreshTokenSecret(jwtRefreshTokenSecret);
        }
    }

    /**
     * 初始化配置文件对象
     *
     * @param data Yaml 对象
     * @return 配置文件对象
     */
    public static Properties initYaml(Object data) {
        final Properties result = new Properties();
        final Object config = YamlUtil.paths(data, "p6e.cloud.auth.client");
        final Map<String, Object> cmap = TransformationUtil.objectToMap(config);
        final String authorizeUrl = TransformationUtil.objectToString(YamlUtil.paths(cmap, "authorizeUrl"));
        final String authorizeTokenUrl = TransformationUtil.objectToString(YamlUtil.paths(cmap, "authorizeTokenUrl"));
        final String authorizeAppId = TransformationUtil.objectToString(YamlUtil.paths(cmap, "authorizeAppId"));
        final String authorizeAppSecret = TransformationUtil.objectToString(YamlUtil.paths(cmap, "authorizeAppSecret"));
        final String authorizeAppRedirectUri = TransformationUtil.objectToString(YamlUtil.paths(cmap, "authorizeAppRedirectUri"));
        final Map<String, Object> authorizeAppExtend = TransformationUtil.objectToMap(YamlUtil.paths(cmap, "authorizeAppExtend"));
        final String jwtAccessTokenSecret = TransformationUtil.objectToString(YamlUtil.paths(cmap, "jwtAccessTokenSecret"));
        final String jwtRefreshTokenSecret = TransformationUtil.objectToString(YamlUtil.paths(cmap, "jwtRefreshTokenSecret"));
        initBase(result, authorizeUrl, authorizeTokenUrl, authorizeAppId, authorizeAppSecret,
                authorizeAppRedirectUri, authorizeAppExtend, jwtAccessTokenSecret, jwtRefreshTokenSecret);
        return result;
    }

    /**
     * 初始化配置文件对象
     *
     * @param properties Properties 对象
     * @return 配置文件对象
     */
    public static Properties initProperties(java.util.Properties properties) {
        final Properties result = new Properties();
        properties = PropertiesUtil.matchProperties("p6e.cloud.auth.client", properties);
        final String authorizeUrl = PropertiesUtil.getStringProperty(properties, "authorizeUrl");
        final String authorizeTokenUrl = PropertiesUtil.getStringProperty(properties, "authorizeTokenUrl");
        final String authorizeAppId = PropertiesUtil.getStringProperty(properties, "authorizeAppId");
        final String authorizeAppSecret = PropertiesUtil.getStringProperty(properties, "authorizeAppSecret");
        final String authorizeAppRedirectUri = PropertiesUtil.getStringProperty(properties, "authorizeAppRedirectUri");
        final Map<String, Object> authorizeAppExtend = PropertiesUtil.getMapProperty(properties, "authorizeAppExtend");
        final String jwtAccessTokenSecret = PropertiesUtil.getStringProperty(properties, "jwtAccessTokenSecret");
        final String jwtRefreshTokenSecret = PropertiesUtil.getStringProperty(properties, "jwtRefreshTokenSecret");
        initBase(result, authorizeUrl, authorizeTokenUrl, authorizeAppId, authorizeAppSecret,
                authorizeAppRedirectUri, authorizeAppExtend, jwtAccessTokenSecret, jwtRefreshTokenSecret);
        return result;
    }
}
