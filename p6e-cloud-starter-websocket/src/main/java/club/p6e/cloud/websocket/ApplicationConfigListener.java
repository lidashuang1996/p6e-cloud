package club.p6e.cloud.websocket;

import club.p6e.coat.websocket.WebSocketMain;
import jakarta.annotation.Nonnull;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author lidashuang
 * @version 1.0
 */
@Component
public class ApplicationConfigListener implements ApplicationListener<WebSocketMain.ApplicationConfigEvent> {

    private final Properties properties;

    public ApplicationConfigListener(Properties properties) {
        this.properties = properties;
    }

    @Override
    public void onApplicationEvent(@Nonnull WebSocketMain.ApplicationConfigEvent event) {
        if (properties.getChannels() != null
                && !properties.getChannels().isEmpty()) {
            WebSocketMain.THREAD_POOL_LENGTH = properties.getThreadPoolLength();
            for (final Properties.Channel channel : properties.getChannels()) {
                WebSocketMain.CONFIGS.add(new WebSocketMain.Config()
                        .setName(channel.getName()).setType(channel.getType()).setPort(channel.getPort()));
            }
        }
    }
}
