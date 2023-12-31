package club.p6e.cloud.gateway;

import org.springframework.stereotype.Component;

/**
 * @author lidashuang
 * @version 1.0
 */
@Component
public class PropertiesRefresher {

    /**
     * 配置文件对象
     */
    private final Properties properties;

    /**
     * 自定义路由定位器
     */
    private final CustomRouteLocator locator;

    /**
     * 构造方法初始化
     *
     * @param properties 配置文件对象
     * @param locator    路由初始化定位器
     */
    public PropertiesRefresher(Properties properties, CustomRouteLocator locator) {
        this.locator = locator;
        this.properties = properties;
        this.locator.refresh(this.properties.getRoutes());
    }

    /**
     * 执行刷新操作
     *
     * @param properties 配置文件对象
     */
    public void execute(Properties properties) {
        this.properties.setLog(properties.getLog());
        this.properties.setReferer(properties.getReferer());
        this.properties.setVersion(properties.getVersion());
        this.properties.setCrossDomain(properties.getCrossDomain());
        this.properties.setRequestHeaderClear(properties.getRequestHeaderClear());
        this.properties.setResponseHeaderOnly(properties.getResponseHeaderOnly());
        this.properties.setRoutes(properties.getRoutes());
        this.locator.refresh(this.properties.getRoutes());
    }

}
