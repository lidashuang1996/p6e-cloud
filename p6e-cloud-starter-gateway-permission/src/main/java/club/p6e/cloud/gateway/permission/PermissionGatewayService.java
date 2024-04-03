package club.p6e.cloud.gateway.permission;

import club.p6e.coat.common.utils.JsonUtil;
import club.p6e.coat.permission.PermissionValidator;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 权限网关服务
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = PermissionGatewayService.class,
        ignored = PermissionGatewayService.class
)
public class PermissionGatewayService {

    /**
     * 用户的信息头
     */
    @SuppressWarnings("ALL")
    private static final String USER_INFO_HEADER = "P6e-User-Info";

    /**
     * 用户的权限头
     */
    @SuppressWarnings("ALL")
    private static final String USER_INFO_PERMISSION = "P6e-User-Permission";

    /**
     * 权限验证器
     */
    private final PermissionValidator validator;

    /**
     * 构造方法初始化
     *
     * @param validator 权限验证器
     */
    public PermissionGatewayService(PermissionValidator validator) {
        this.validator = validator;
    }

    public Mono<ServerWebExchange> execute(ServerWebExchange exchange) {
        final String path = exchange.getRequest().getPath().value();
        final String method = exchange.getRequest().getMethod().name().toUpperCase();
        final String user = exchange.getRequest().getHeaders().getFirst(USER_INFO_HEADER);
        final UserModel um = JsonUtil.fromJson(user, UserModel.class);
        if (um == null || um.getPermissions() == null || um.getPermissions().isEmpty()) {
            return Mono.empty();
        } else {
            return validator
                    .execute(path, method, um.getPermissions())
                    .flatMap(permission -> Mono.just(exchange.mutate().request(
                            exchange.getRequest().mutate().header(
                                    USER_INFO_PERMISSION, JsonUtil.toJson(permission)
                            ).build()
                    ).build()));
        }
    }


    @Data
    @Accessors(chain = true)
    private static class UserModel implements Serializable {
        private List<String> permissions = new ArrayList<>();
    }

}
