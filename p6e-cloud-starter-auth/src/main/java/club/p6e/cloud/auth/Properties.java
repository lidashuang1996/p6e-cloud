package club.p6e.cloud.auth;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * Properties
 * Since the properties in the configuration file are read and executed at
 * program startup and are not accessed afterward, the refresh function does not need to exist.
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