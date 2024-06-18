package club.p6e.cloud.gateway.permission;

import club.p6e.coat.common.controller.BaseWebFluxController;
import club.p6e.coat.common.utils.JsonUtil;
import club.p6e.coat.permission.PermissionValidator;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    protected static final String USER_INFO_HEADER = "P6e-User-Info";

    /**
     * 用户的权限头
     */
    @SuppressWarnings("ALL")
    protected static final String USER_INFO_PERMISSION_HEADER = "P6e-User-Permission";

    /**
     * 用户项目信息的头部
     */
    @SuppressWarnings("ALL")
    private static final String USER_PROJECT_HEADER = "P6e-User-Project";

    /**
     * 用户组织信息的头部
     */
    @SuppressWarnings("ALL")
    private static final String USER_ORGANIZATION_HEADER = "P6e-User-Organization";


    /**
     * 权限验证器
     */
    protected final PermissionValidator validator;

    /**
     * 构造方法初始化
     *
     * @param validator 权限验证器
     */
    public PermissionGatewayService(PermissionValidator validator) {
        this.validator = validator;
    }

    public Mono<ServerWebExchange> execute(ServerWebExchange exchange) {
        final ServerHttpRequest request = exchange.getRequest();
        final String path = request.getPath().value();
        final String method = request.getMethod().name().toUpperCase();
        final String user = BaseWebFluxController.getHeader(request, USER_INFO_HEADER);
        final String project = BaseWebFluxController.getHeader(request, USER_PROJECT_HEADER);
        final String organization = BaseWebFluxController.getHeader(request, USER_ORGANIZATION_HEADER);
        System.out.println("path >>>> " + path);
        System.out.println("method >>>> " + method);
        System.out.println("user >>>> " + user);
        System.out.println("project >>>> " + project);
        System.out.println("organization >>>> " + organization);
        if (user != null && !user.isEmpty()
                && project != null && !project.isEmpty()
                && organization != null && !organization.isEmpty()) {
            final UserModel um = JsonUtil.fromJson(user, UserModel.class);
            System.out.println("um >>>> " + um);
            if (um != null
                    && um.getPermission() != null
                    && um.getPermission().get(project) != null) {
                return validator
                        .execute(path, method, um.getPermission().get(project).get("group"))
                        .flatMap(permission -> Mono.just(exchange.mutate().request(
                                exchange.getRequest().mutate().header(USER_INFO_PERMISSION_HEADER, JsonUtil.toJson(permission)).build()
                        ).build()));
            }
        }
        return Mono.empty();
    }

    @Data
    @Accessors(chain = true)
    private static class UserModel implements Serializable {
        private Map<String, Map<String, List<String>>> permission = new HashMap<>();
    }

}
