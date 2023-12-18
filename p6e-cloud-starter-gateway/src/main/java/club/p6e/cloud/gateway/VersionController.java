package club.p6e.cloud.gateway;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lidashuang
 * @version 1.0
 */
@RestController
@RequestMapping("/__version__")
public class VersionController {

    private final Properties properties;

    public VersionController(Properties properties) {
        this.properties = properties;
    }

    @RequestMapping()
    public String version() {
        return properties.getVersion();
    }

}
