package club.p6e.cloud.common;

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
@Component(value = "club.p6e.cloud.common.PropertiesRefresher")
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
     * Execute Refresh
     *
     * @param properties Properties object
     */
    @SuppressWarnings("ALL")
    public void execute(Properties properties) {
        LOGGER.info("[ NEW PROPERTIES ] ({}) >>> {}", properties.getClass(), JsonUtil.toJson(properties));
        this.properties.setVersion(properties.getVersion());
        this.properties.setSecurity(properties.getSecurity());
        this.properties.setCrossDomain(properties.getCrossDomain());
        this.properties.setSnowflake(properties.getSnowflake());
    }

}
