package club.p6e.cloud.file;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 配置文件
 *
 * @author lidashuang
 * @version 1.0
 */
@Primary
@Component
@ConfigurationProperties(prefix = "p6e.cloud.file")
public class Properties extends club.p6e.coat.file.Properties implements Serializable {
}