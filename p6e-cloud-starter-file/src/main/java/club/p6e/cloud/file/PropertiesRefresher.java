package club.p6e.cloud.file;

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

    private final club.p6e.coat.file.Properties fileProperties;

    /**
     * 构造方法初始化
     *
     * @param properties 配置文件对象
     */
    public PropertiesRefresher(Properties properties, club.p6e.coat.file.Properties fileProperties) {
        this.properties = properties;
        this.fileProperties = fileProperties;
        copy(properties, fileProperties);
    }

    /**
     * 执行刷新操作
     *
     * @param properties 配置文件对象
     */
    public void execute(Properties properties) {
        this.properties.setVersion(properties.getVersion());
        this.properties.setReferer(properties.getReferer());
        this.properties.setCrossDomain(properties.getCrossDomain());
        this.properties.setSliceUpload(properties.getSliceUpload());
        this.properties.setUploads(properties.getUploads());
        this.properties.setResources(properties.getResources());
        this.properties.setDownloads(properties.getDownloads());
        copy(this.properties, this.fileProperties);
    }

    private void copy(club.p6e.cloud.file.Properties properties, club.p6e.coat.file.Properties fileProperties) {
        fileProperties.setVersion(properties.getVersion());
        fileProperties.setReferer(properties.getReferer());
        fileProperties.setCrossDomain(properties.getCrossDomain());
        fileProperties.setSliceUpload(properties.getSliceUpload());
        fileProperties.setUploads(properties.getUploads());
        fileProperties.setResources(properties.getResources());
        fileProperties.setDownloads(properties.getDownloads());
    }
}
