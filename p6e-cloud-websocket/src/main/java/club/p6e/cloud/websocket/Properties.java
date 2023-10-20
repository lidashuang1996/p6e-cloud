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


    private int threadPoolLength = 15;
    private List<Config> configs = new ArrayList<>();

    @Data
    @Accessors(chain = true)
    public static class Config implements Serializable {
        private String name;
        private String type;
        private Integer port;
    }

}
