package club.p6e.cloud.websocket;

import club.p6e.coat.websocket.WebSocketMain;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Component;

/**
 * @author lidashuang
 * @version 1.0
 */
@Component
public class ApplicationListener implements
        org.springframework.context.ApplicationListener<WebSocketMain.ApplicationConfigEvent> {

    private final Properties properties;

    public ApplicationListener(Properties properties) {
        this.properties = properties;
    }

    @Override
    public void onApplicationEvent(@Nonnull WebSocketMain.ApplicationConfigEvent event) {
        if (properties.getConfigs() != null
                && !properties.getConfigs().isEmpty()) {
            WebSocketMain.THREAD_POOL_LENGTH = properties.getThreadPoolLength();
            for (final Properties.Config config : properties.getConfigs()) {
                WebSocketMain.CONFIGS.add(new WebSocketMain.Config()
                        .setName(config.getName()).setType(config.getType()).setPort(config.getPort()));
            }
        }
    }
}
