package club.p6e.cloud.file;

import club.p6e.coat.common.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 配置刷新器
 *
 * @author lidashuang
 * @version 1.0
 */
@Component("club.p6e.cloud.file.PropertiesRefresher")
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
        execute(properties);
    }

    /**
     * Execute Refresh
     *
     * @param properties Properties object
     */
    @SuppressWarnings("ALL")
    public void execute(Properties properties) {
        LOGGER.info("[ NEW PROPERTIES ] ({}) >>> {}", properties.getClass(), JsonUtil.toJson(properties));
        this.properties.setSecret(properties.getSecret());
        this.properties.setSliceUpload(properties.getSliceUpload());
        this.properties.setUploads(properties.getUploads());
        this.properties.setResources(properties.getResources());
        this.properties.setDownloads(properties.getDownloads());
    }

}
