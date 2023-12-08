package club.p6e.cloud.gateway;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigChangeEvent;
import com.alibaba.nacos.client.config.listener.impl.AbstractConfigChangeListener;
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lidashuang
 * @version 1.0
 */
public class NacosPropertiesRefresher implements ApplicationListener<ApplicationReadyEvent> {

    @Value(value = "${spring.profiles.active}")
    private String active;

    @Value(value = "${spring.application.name:DEFAULT_GROUP}")
    private String name;

    @Value(value = "${spring.cloud.nacos.discovery.group:DEFAULT_GROUP}")
    private String group;

    @Value(value = "${spring.cloud.nacos.config.file-extension:properties}")
    private String fileExtension;

    private final NacosConfigManager manager;
    private final PropertiesRefresher refresher;

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
                                config(manager.getConfigService().getConfig(dataId, groupId, 5000L));
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

    protected void config(String content) {
        refresher.execute(propertiesToMapData(content));
    }

    protected Map<String, String> propertiesToMapData(String content) {
        try (final StringReader reader = new StringReader(content)) {
            final Map<String, String> result = new HashMap<>();
            final java.util.Properties properties = new java.util.Properties();
            properties.load(reader);
            properties.forEach((k, v) -> result.put(String.valueOf(k), String.valueOf(v)));
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
