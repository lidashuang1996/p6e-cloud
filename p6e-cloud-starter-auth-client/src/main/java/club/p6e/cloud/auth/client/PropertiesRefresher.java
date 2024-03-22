package club.p6e.cloud.auth.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author lidashuang
 * @version 1.0
 */
@Component(value = "club.p6e.cloud.auth.client.PropertiesRefresher")
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
    }

}
