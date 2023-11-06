package club.p6e.cloud.gateway.auth;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author lidashuang
 * @version 1.0
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(AuthGatewayFilterFactory.class)
public @interface EnableP6eAuthPermission {
}
