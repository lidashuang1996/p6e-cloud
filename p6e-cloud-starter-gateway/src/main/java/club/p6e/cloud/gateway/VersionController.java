package club.p6e.cloud.gateway;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 版本的查看控制器
 *
 * @author lidashuang
 * @version 1.0
 */
@RestController
@RequestMapping("/version")
public class VersionController {

    /**
     * 配置文件对象
     */
    private final Properties properties;

    /**
     * 构造方法初始化
     *
     * @param properties 配置文件对象
     */
    public VersionController(Properties properties) {
        this.properties = properties;
    }

    /**
     * 获取版本信息
     *
     * @return 版本信息
     */
    @RequestMapping()
    public String version() {
        return "club.p6e.cloud.gateway@version:" + properties.getVersion();
    }

}
