package club.p6e.cloud.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author lidashuang
 * @version 1.0
 */
@Component(value = "club.p6e.cloud.common.PropertiesRefresher")
public class PropertiesRefresher {

    /**
     * 注入日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesRefresher.class);

    /**
     * 配置文件对象
     */
    private final Properties properties;

    private final club.p6e.coat.common.Properties commonProperties;

    /**
     * 构造方法初始化
     *
     * @param properties 配置文件对象
     */
    public PropertiesRefresher(Properties properties, club.p6e.coat.common.Properties commonProperties) {
        this.properties = properties;
        this.commonProperties = commonProperties;
        copy(properties, commonProperties);
    }

    /**
     * 执行刷新操作
     *
     * @param properties 配置文件对象
     */
    public void execute(Properties properties) {
        LOGGER.info("[NEW PROPERTIES] (" + properties.getClass() + ") >>>> " + properties);
        this.properties.setVersion(properties.getVersion());
        this.properties.setSecurity(properties.getSecurity());
        this.properties.setCrossDomain(properties.getCrossDomain());
        this.properties.setSnowflake(properties.getSnowflake());
        copy(this.properties, this.commonProperties);
    }

    private void copy(Properties properties, club.p6e.coat.common.Properties commonProperties) {
        commonProperties.setVersion(properties.getVersion());
        commonProperties.getSecurity().setEnable(properties.getSecurity().isEnable());
        commonProperties.getSecurity().getVouchers().clear();
        commonProperties.getSecurity().getVouchers().addAll(properties.getSecurity().getVouchers());
        commonProperties.getCrossDomain().setEnable(properties.getCrossDomain().isEnable());
        commonProperties.getCrossDomain().getWhiteList().clear();
        commonProperties.getCrossDomain().getWhiteList().addAll(properties.getCrossDomain().getWhiteList());
        commonProperties.getSnowflake().clear();
        commonProperties.getSnowflake().putAll(properties.getSnowflake());
    }

}
