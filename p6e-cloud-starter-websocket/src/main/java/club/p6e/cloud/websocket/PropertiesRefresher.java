package club.p6e.cloud.websocket;

import club.p6e.coat.websocket.WebSocketMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lidashuang
 * @version 1.0
 */
@Component(value = "club.p6e.cloud.websocket.PropertiesRefresher")
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
     * WebSocket 主类对象
     */
    private final WebSocketMain webSocketMain;

    /**
     * 构造方法初始化
     *
     * @param properties    配置文件对象
     * @param webSocketMain WebSocket 主类对象
     */
    public PropertiesRefresher(Properties properties, WebSocketMain webSocketMain) {
        this.properties = properties;
        this.webSocketMain = webSocketMain;
        final List<WebSocketMain.Config> configs = new ArrayList<>();
        if (properties.getChannels() != null
                && !properties.getChannels().isEmpty()) {
            for (final Properties.Channel channel : properties.getChannels()) {
                configs.add(new WebSocketMain.Config()
                        .setName(channel.getName())
                        .setType(channel.getType())
                        .setPort(channel.getPort())
                );
            }
        }
        webSocketMain.setConfig(configs);
        webSocketMain.setThreadPoolLength(properties.getThreadPoolLength());
        webSocketMain.reset();
    }

    /**
     * 执行刷新操作
     *
     * @param properties 配置文件对象
     */
    public void execute(Properties properties) {
        LOGGER.info("[NEW PROPERTIES] ({}) >>>> {}", properties.getClass(), properties);
        this.properties.setChannels(properties.getChannels());
        this.properties.setThreadPoolLength(properties.getThreadPoolLength());
        final List<WebSocketMain.Config> configs = new ArrayList<>();
        if (this.properties.getChannels() != null
                && !properties.getChannels().isEmpty()) {
            for (final Properties.Channel channel : this.properties.getChannels()) {
                configs.add(new WebSocketMain.Config()
                        .setName(channel.getName())
                        .setType(channel.getType())
                        .setPort(channel.getPort())
                );
            }
        }
        webSocketMain.setConfig(configs);
        webSocketMain.setThreadPoolLength(this.properties.getThreadPoolLength());
        webSocketMain.reset();
    }

}
