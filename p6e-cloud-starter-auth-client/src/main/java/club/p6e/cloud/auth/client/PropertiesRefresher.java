package club.p6e.cloud.auth.client;

import club.p6e.coat.common.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Properties Refresher
 *
 * @author lidashuang
 * @version 1.0
 */
@Component(value = "club.p6e.cloud.auth.client.PropertiesRefresher")
public class PropertiesRefresher {

    /**
     * Inject log objects
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesRefresher.class);

    /**
     * Properties object
     */
    private final Properties properties;

    /**
     * Constructor initializers
     *
     * @param properties Properties object
     */
    public PropertiesRefresher(Properties properties) {
        this.properties = properties;
    }

    /**
     * Execute refresh
     *
     * @param properties Properties object
     */
    @SuppressWarnings("ALL")
    public void execute(Properties properties) {
        LOGGER.info("[ NEW PROPERTIES ] ({}) >>> {}", properties.getClass(), JsonUtil.toJson(properties));
        this.properties.setAuthorizeUrl(properties.getAuthorizeUrl());
        this.properties.setAuthorizeTokenUrl(properties.getAuthorizeTokenUrl());
        this.properties.setAuthorizeAppId(properties.getAuthorizeAppId());
        this.properties.setAuthorizeAppSecret(properties.getAuthorizeAppSecret());
        this.properties.setAuthorizeAppRedirectUri(properties.getAuthorizeAppRedirectUri());
        this.properties.setAuthorizeAppExtend(properties.getAuthorizeAppExtend());
        this.properties.setJwtAccessTokenSecret(properties.getJwtAccessTokenSecret());
        this.properties.setJwtRefreshTokenSecret(properties.getJwtRefreshTokenSecret());
    }

}
