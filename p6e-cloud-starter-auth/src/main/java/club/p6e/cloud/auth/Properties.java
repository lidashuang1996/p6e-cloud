package club.p6e.cloud.auth;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 配置文件
 * 由于配置文件中属性在程序启动时候就读取执行后面不在访问，故刷新这个功能不需要存在
 *
 * @author lidashuang
 * @version 1.0
 */
@Data
@Primary
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "p6e.cloud.auth")
@Component(value = "club.p6e.cloud.auth.Properties")
public class Properties extends club.p6e.coat.auth.Properties implements Serializable {
}