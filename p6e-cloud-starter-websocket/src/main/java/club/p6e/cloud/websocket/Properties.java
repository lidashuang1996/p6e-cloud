package club.p6e.cloud.websocket;

import club.p6e.coat.common.utils.PropertiesUtil;
import club.p6e.coat.common.utils.TransformationUtil;
import club.p6e.coat.common.utils.YamlUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lidashuang
 * @version 1.0
 */
@Data
@Accessors(chain = true)
@Component(value = "club.p6e.cloud.websocket.Properties")
@ConfigurationProperties(prefix = "p6e.cloud.websocket")
public class Properties implements Serializable {

    /**
     * 默认的线程池大小
     */
    private int threadPoolLength = 15;

    /**
     * 频道配置信息
     */
    private List<Channel> channels = new ArrayList<>();

    /**
     * 认证的密钥
     */
    private String secret;

    /**
     * 初始化配置文件对象
     *
     * @param data Yaml 对象
     * @return 配置文件对象
     */
    public static Properties initYaml(Object data) {
        final Properties result = new Properties();
        final Object config = YamlUtil.paths(data, "p6e.cloud.websocket");
        final Map<String, Object> cmap = TransformationUtil.objectToMap(config);
        final Integer threadPoolLength = TransformationUtil.objectToInteger(YamlUtil.paths(cmap, "threadPoolLength"));
        final List<Object> channelList = TransformationUtil.objectToList(YamlUtil.paths(cmap, "channels"));
        if (threadPoolLength != null) {
            result.setThreadPoolLength(threadPoolLength);
        }
        if (channelList != null) {
            final List<Channel> channels = new ArrayList<>();
            for (final Object item : channelList) {
                final Map<String, Object> imap = TransformationUtil.objectToMap(item);
                final String name = TransformationUtil.objectToString(YamlUtil.paths(imap, "name"));
                final String type = TransformationUtil.objectToString(YamlUtil.paths(imap, "type"));
                final Integer port = TransformationUtil.objectToInteger(YamlUtil.paths(imap, "port"));
                if (name != null && type != null && port != null) {
                    channels.add(new Channel().setName(name).setType(type).setPort(port));
                }
            }
            result.setChannels(channels);
        }
        return result;
    }

    /**
     * 初始化配置文件对象
     *
     * @param properties Properties 对象
     * @return 配置文件对象
     */
    public static Properties initProperties(java.util.Properties properties) {
        final Properties result = new Properties();
        properties = PropertiesUtil.matchProperties("p6e.cloud.websocket", properties);
        final Integer threadPoolLength = PropertiesUtil.getIntegerProperty(properties, "threadPoolLength");
        if (threadPoolLength != null) {
            result.setThreadPoolLength(threadPoolLength);
        }
        final List<java.util.Properties> channelList =
                PropertiesUtil.getListPropertiesProperty(properties, "channels");
        if (channelList != null) {
            final List<Channel> channels = new ArrayList<>();
            for (final java.util.Properties item : channelList) {
                final String name = PropertiesUtil.getStringProperty(item, "name");
                final String type = PropertiesUtil.getStringProperty(item, "type");
                final Integer port = PropertiesUtil.getIntegerProperty(item, "port");
                if (name != null && type != null && port != null) {
                    channels.add(new Channel().setName(name).setType(type).setPort(port));
                }
            }
            result.setChannels(channels);
        }
        return result;
    }

    @Data
    @Accessors(chain = true)
    public static class Channel implements Serializable {

        /**
         * 频道名称
         */
        private String name;

        /**
         * 频道数据类型
         */
        private String type;

        /**
         * 频道启动端口
         */
        private Integer port;

    }

}
