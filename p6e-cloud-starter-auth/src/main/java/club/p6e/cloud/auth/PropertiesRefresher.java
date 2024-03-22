package club.p6e.cloud.auth;

import club.p6e.coat.auth.AuthJsonWebTokenCipher;
import club.p6e.coat.common.utils.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author lidashuang
 * @version 1.0
 */
@Component(value = "club.p6e.cloud.auth.PropertiesRefresher")
public class PropertiesRefresher {

    /**
     * 注入日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesRefresher.class);

    /**
     * 配置文件对象
     */
    private final Properties properties;

    /**
     * 构造方法初始化
     *
     * @param properties 配置文件对象
     */
    public PropertiesRefresher(Properties properties) {
        this.properties = properties;
    }

    /**
     * 执行刷新操作
     *
     * @param properties 配置文件对象
     */
    public void execute(Properties properties) {
        LOGGER.info("[NEW PROPERTIES] (" + properties.getClass() + ") >>>> " + properties);
        this.properties.setRedirectIndexPage(properties.isRedirectIndexPage());
        this.properties.setRedirectIndexPagePath(properties.getRedirectIndexPagePath());
        this.properties.setJwtAccessTokenSecret(properties.getJwtAccessTokenSecret());
        this.properties.setJwtRefreshTokenSecret(properties.getJwtRefreshTokenSecret());

        try {
            final AuthJsonWebTokenCipher cipher = SpringUtil.getBean(AuthJsonWebTokenCipher.class);
            cipher.setAccessTokenSecret(this.properties.getJwtAccessTokenSecret());
            cipher.setRefreshTokenSecret(this.properties.getJwtRefreshTokenSecret());
        } catch (Exception e) {
            // ignore
        }

        this.properties.getLogin().setEnable(properties.getLogin().isEnable());
        this.properties.getLogin().getQrCode().setEnable(properties.getLogin().getQrCode().isEnable());
        this.properties.getLogin().getAccountPassword().setEnable(properties.getLogin().getAccountPassword().isEnable());
        this.properties.getLogin().getAccountPassword().setEnableTransmissionEncryption(properties.getLogin().getAccountPassword().isEnableTransmissionEncryption());
        this.properties.getLogin().getVerificationCode().setEnable(properties.getLogin().getVerificationCode().isEnable());
        properties.getLogin().getOthers().forEach((k, v) -> {
            final club.p6e.coat.auth.Properties.Login.Other other = this.properties.getLogin().getOthers().get(k);
            if (other != null) {
                other.setEnable(v.isEnable());
                v.getConfig().forEach((vk, vv) -> other.getConfig().put(vk, vv));
            }
        });

        this.properties.getOauth2().setEnable(properties.getOauth2().isEnable());
        this.properties.getOauth2().getClient().setEnable(properties.getOauth2().getClient().isEnable());
        this.properties.getOauth2().getPassword().setEnable(properties.getOauth2().getPassword().isEnable());
        this.properties.getOauth2().getAuthorizationCode().setEnable(properties.getOauth2().getAuthorizationCode().isEnable());
        this.properties.getRegister().setEnable(properties.getRegister().isEnable());
        this.properties.getRegister().setEnableOtherLoginBinding(properties.getRegister().isEnableOtherLoginBinding());
        this.properties.getForgotPassword().setEnable(properties.getForgotPassword().isEnable());
    }

}
