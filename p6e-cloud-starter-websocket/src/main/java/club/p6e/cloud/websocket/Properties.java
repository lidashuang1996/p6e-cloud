package club.p6e.cloud.websocket;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lidashuang
 * @version 1.0
 */
@Data
@Component
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
