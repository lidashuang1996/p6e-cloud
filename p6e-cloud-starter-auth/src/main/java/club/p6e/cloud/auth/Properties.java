package club.p6e.cloud.auth;

import club.p6e.coat.common.utils.PropertiesUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @author lidashuang
 * @version 1.0
 */
@Data
@Primary
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Component(value = "club.p6e.cloud.auth.Properties")
@ConfigurationProperties(prefix = "p6e.cloud.cloud.auth")
public class Properties extends club.p6e.coat.auth.Properties implements Serializable {

    /**
     * 初始化配置文件对象
     *
     * @param data Yaml 对象
     * @return 配置文件对象
     */
    public static Properties initYaml(Object data) {
        final Properties result = new Properties();
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
        properties = PropertiesUtil.matchProperties("p6e.cloud.auth", properties);
        final Boolean redirectIndexPage = PropertiesUtil.getBooleanProperty(properties, "redirectIndexPage");
        if (redirectIndexPage != null) {
            result.setRedirectIndexPage(redirectIndexPage);
        }
        final String redirectIndexPagePath = PropertiesUtil.getStringProperty(properties, "redirectIndexPagePath");
        if (redirectIndexPagePath != null) {
            result.setRedirectIndexPagePath(redirectIndexPagePath);
        }
        final String jwtAccessTokenSecret = PropertiesUtil.getStringProperty(properties, "jwtAccessTokenSecret");
        if (jwtAccessTokenSecret != null) {
            result.setJwtAccessTokenSecret(jwtAccessTokenSecret);
        }
        final String jwtRefreshTokenSecret = PropertiesUtil.getStringProperty(properties, "jwtRefreshTokenSecret");
        if (jwtRefreshTokenSecret != null) {
            result.setJwtRefreshTokenSecret(jwtRefreshTokenSecret);
        }
        final java.util.Properties loginProperties = PropertiesUtil.matchProperties("login", properties);
        final java.util.Properties qcProperties = PropertiesUtil.matchProperties("qrCode", properties);
        final java.util.Properties apProperties = PropertiesUtil.matchProperties("accountPassword", properties);
        final java.util.Properties acProperties = PropertiesUtil.matchProperties("verificationCode", properties);
        final Boolean enableLogin = PropertiesUtil.getBooleanProperty(loginProperties, "enable");
        return result;
    }

    @Data
    @Accessors(chain = true)
    public static class Channel implements Serializable {
        /**
         * 频道名称
         */
        private String name;

        /**
         * 频道数据类型
         */
        private String type;

        /**
         * 频道启动端口
         */
        private Integer port;
    }

}
