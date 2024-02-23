package club.p6e.cloud.gateway;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigChangeEvent;
import com.alibaba.nacos.client.config.listener.impl.AbstractConfigChangeListener;
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.yaml.snakeyaml.Yaml;

import java.io.StringReader;

/**
 * Nacos 配置文件属性刷新器
 *
 * @author lidashuang
 * @version 1.0
 */
public class NacosPropertiesRefresher implements ApplicationListener<ApplicationReadyEvent> {

    @Value(value = "${spring.profiles.active}")
    protected String active;

    @Value(value = "${spring.application.name:DEFAULT_NAME}")
    protected String name;

    @Value(value = "${spring.cloud.nacos.discovery.group:DEFAULT_GROUP}")
    protected String group;

    @Value(value = "${spring.cloud.nacos.config.file-extension:properties}")
    protected String fileExtension;

    protected final NacosConfigManager manager;
    protected final PropertiesRefresher refresher;

    public NacosPropertiesRefresher(NacosConfigManager manager, PropertiesRefresher refresher) {
        this.manager = manager;
        this.refresher = refresher;
    }

    @Override
    public void onApplicationEvent(@Nonnull ApplicationReadyEvent event) {
        try {
            final String dataId = getDataId();
            final String groupId = getGroupId();
            manager.getConfigService()
                    .addListener(dataId, groupId, new AbstractConfigChangeListener() {
                        @Override
                        public void receiveConfigChange(ConfigChangeEvent configChangeEvent) {
                            try {
                                config(fileExtension.toLowerCase(), manager.getConfigService().getConfig(dataId, groupId, 5000L));
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected String getDataId() {
        return name + (active == null ? "" : ("-" + active)) + "." + fileExtension;
    }

    protected String getGroupId() {
        return group;
    }

    protected void config(String format, String content) {
        switch (format) {
            case "yaml":
                try {
                    refresher.execute(Properties.initYaml(new Yaml().load(content)));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                break;
            case "properties":
                final java.util.Properties properties = new java.util.Properties();
                try {
                    properties.load(new StringReader(content));
                    refresher.execute(Properties.initProperties(properties));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                break;
            default:
                break;
        }
    }

}
