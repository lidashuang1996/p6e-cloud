package club.p6e.cloud.gateway;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 配置文件
 *
 * @author lidashuang
 * @version 1.0
 */
@Data
@Component
@Accessors(chain = true)
@ConfigurationProperties(prefix = "p6e.cloud.gateway")
public class Properties implements Serializable {

    /**
     * 版本号
     */
    private String version = "unknown";

    /**
     * Log
     */
    private Log log = new Log();

    /**
     * Referer
     */
    private Referer referer = new Referer();

    /**
     * Cross Domain
     */
    private CrossDomain crossDomain = new CrossDomain();

    /**
     * 请求头清除
     */
    private List<String> requestHeaderClear = new ArrayList<>();

    /**
     * 返回头唯一
     */
    private List<String> responseHeaderOnly = new ArrayList<>();

    /**
     * 路由
     */
    private List<RouteDefinition> routes = new ArrayList<>();

    /**
     * Log
     */
    @Data
    @Accessors(chain = true)
    public static class Log implements Serializable {
        /**
         * 是否启动
         */
        private boolean enabled = false;

        /**
         * 是否启动打印
         */
        private boolean details = false;
    }

    /**
     * Referer
     */
    @Data
    @Accessors(chain = true)
    public static class Referer implements Serializable {
        /**
         * 是否启动
         */
        private boolean enabled = false;

        /**
         * 白名单
         */
        private String[] whiteList = new String[]{"*"};
    }

    /**
     * Cross Domain
     */
    @Data
    @Accessors(chain = true)
    public static class CrossDomain implements Serializable {
        /**
         * 是否启动
         */
        private boolean enabled = false;
    }

}