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
 * Properties
 *
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

    /**
     * INIT BASE
     */
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
     * INIT YAML
     */
    @SuppressWarnings("ALL")
    public static Properties initYaml(Object data) {
        final Properties result = new Properties();
        final Object config = YamlUtil.paths(data, "p6e.cloud.auth.client");
        final Map<String, Object> cmap = TransformationUtil.objectToMap(config);
        final String authorizeUrl = TransformationUtil.objectToString(YamlUtil.paths(cmap, "authorize-url"));
        final String authorizeTokenUrl = TransformationUtil.objectToString(YamlUtil.paths(cmap, "authorize-token-url"));
        final String authorizeAppId = TransformationUtil.objectToString(YamlUtil.paths(cmap, "authorize-app-id"));
        final String authorizeAppSecret = TransformationUtil.objectToString(YamlUtil.paths(cmap, "authorize-app-secret"));
        final String authorizeAppRedirectUri = TransformationUtil.objectToString(YamlUtil.paths(cmap, "authorize-app-redirect-uri"));
        final Map<String, Object> authorizeAppExtend = TransformationUtil.objectToMap(YamlUtil.paths(cmap, "authorize-app-extend"));
        final String jwtAccessTokenSecret = TransformationUtil.objectToString(YamlUtil.paths(cmap, "jwt-access-token-secret"));
        final String jwtRefreshTokenSecret = TransformationUtil.objectToString(YamlUtil.paths(cmap, "jwt-refresh-token-secret"));
        initBase(result, authorizeUrl, authorizeTokenUrl, authorizeAppId, authorizeAppSecret,
                authorizeAppRedirectUri, authorizeAppExtend, jwtAccessTokenSecret, jwtRefreshTokenSecret);
        return result;
    }

    /**
     * INIT PROPERTIES
     */
    @SuppressWarnings("ALL")
    public static Properties initProperties(java.util.Properties properties) {
        final Properties result = new Properties();
        properties = PropertiesUtil.matchProperties("p6e.cloud.auth.client", properties);
        final String authorizeUrl = PropertiesUtil.getStringProperty(properties, "authorize-url");
        final String authorizeTokenUrl = PropertiesUtil.getStringProperty(properties, "authorize-token-url");
        final String authorizeAppId = PropertiesUtil.getStringProperty(properties, "authorize-app-id");
        final String authorizeAppSecret = PropertiesUtil.getStringProperty(properties, "authorize-app-secret");
        final String authorizeAppRedirectUri = PropertiesUtil.getStringProperty(properties, "authorize-app-redirect-uri");
        final Map<String, Object> authorizeAppExtend = PropertiesUtil.getMapProperty(properties, "authorize-app-extend");
        final String jwtAccessTokenSecret = PropertiesUtil.getStringProperty(properties, "jwt-access-token-secret");
        final String jwtRefreshTokenSecret = PropertiesUtil.getStringProperty(properties, "jwt-refresh-token-secret");
        initBase(result, authorizeUrl, authorizeTokenUrl, authorizeAppId, authorizeAppSecret,
                authorizeAppRedirectUri, authorizeAppExtend, jwtAccessTokenSecret, jwtRefreshTokenSecret);
        return result;
    }

}
