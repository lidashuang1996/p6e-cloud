package club.p6e.cloud.gateway;

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
@Component("club.p6e.cloud.gateway.PropertiesRefresher")
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
     * CustomRouteLocator object
     */
    private final CustomRouteLocator locator;

    /**
     * Constructor initializers
     *
     * @param properties Properties object
     * @param locator    CustomRouteLocator object
     */
    public PropertiesRefresher(Properties properties, CustomRouteLocator locator) {
        this.locator = locator;
        this.properties = properties;
        this.locator.refresh(this.properties.getRoutes());
    }

    /**
     * Execute refresh
     *
     * @param properties Properties object
     */
    @SuppressWarnings("ALL")
    public void execute(Properties properties) {
        LOGGER.info("[ NEW PROPERTIES ] ({}) >>> {}", properties.getClass(), JsonUtil.toJson(properties));
        this.properties.setLog(properties.getLog());
        this.properties.setRequestHeaderClear(properties.getRequestHeaderClear());
        this.properties.setResponseHeaderOnly(properties.getResponseHeaderOnly());
        this.properties.setRoutes(properties.getRoutes());
        // Use custom gateway routing locators to perform new routing configurations
        this.locator.refresh(this.properties.getRoutes());
    }

}
