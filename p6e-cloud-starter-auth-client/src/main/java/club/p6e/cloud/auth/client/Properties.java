package club.p6e.cloud.auth.client;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @author lidashuang
 * @version 1.0
 */
@Data
@Primary
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Component(value = "club.p6e.cloud.auth.client.Properties")
@ConfigurationProperties(prefix = "p6e.cloud.cloud.auth.client")
public class Properties extends club.p6e.coat.auth.client.Properties implements Serializable {

    /**
     * 初始化配置文件对象
     *
     * @param data Yaml 对象
     * @return 配置文件对象
     */
    public static Properties initYaml(Object data) {
        final Properties result = new Properties();
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
        return result;
    }

}
