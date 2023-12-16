package club.p6e.cloud.gateway.permission;

import club.p6e.coat.permission.EnableP6ePermission;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author lidashuang
 * @version 1.0
 */
@Documented
@EnableP6ePermission
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({
        PermissionGatewayService.class,
        PermissionGatewayFilterFactory.class
})
public @interface EnableP6eGatewayPermission {
}
