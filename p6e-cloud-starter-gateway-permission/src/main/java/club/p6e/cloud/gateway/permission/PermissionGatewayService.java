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
 * Permission Gateway Service
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
     * P6e User Info Header Name
     */
    @SuppressWarnings("ALL")
    protected static final String USER_INFO_HEADER = "P6e-User-Info";

    /**
     * P6e User Permission Header Name
     */
    @SuppressWarnings("ALL")
    protected static final String USER_INFO_PERMISSION_HEADER = "P6e-User-Permission";

    /**
     * P6e User Project Header Name
     */
    @SuppressWarnings("ALL")
    private static final String USER_PROJECT_HEADER = "P6e-User-Project";

    /**
     * P6e User Organization Header Name
     */
    @SuppressWarnings("ALL")
    private static final String USER_ORGANIZATION_HEADER = "P6e-User-Organization";

    /**
     * Permission validator object
     */
    protected final PermissionValidator validator;

    /**
     * Constructor initializers
     *
     * @param validator Permission validator object
     */
    public PermissionGatewayService(PermissionValidator validator) {
        this.validator = validator;
    }

    /**
     * Permission execute
     *
     * @param exchange ServerWebExchange object
     * @return Mono<ServerWebExchange> ServerWebExchange object
     */
    public Mono<ServerWebExchange> execute(ServerWebExchange exchange) {
        final ServerHttpRequest request = exchange.getRequest();
        final String path = request.getPath().value();
        final String method = request.getMethod().name().toUpperCase();
        final String user = BaseWebFluxController.getHeader(request, USER_INFO_HEADER);
        final String project = BaseWebFluxController.getHeader(request, USER_PROJECT_HEADER);
        final String organization = BaseWebFluxController.getHeader(request, USER_ORGANIZATION_HEADER);
        if (user == null || user.isEmpty()) {
            return validator
                    .execute(path, method, project, List.of("*"))
                    .flatMap(permission -> {
                        if ("@PERMISSION-IGNORE".equalsIgnoreCase(permission.getMark())) {
                            return Mono.just(exchange.mutate().request(
                                    exchange.getRequest().mutate().header(USER_INFO_PERMISSION_HEADER, JsonUtil.toJson(permission)).build()
                            ).build());
                        } else {
                            return Mono.empty();
                        }
                    });
        } else {
            final UserModel um = JsonUtil.fromJson(user, UserModel.class);
            if (project == null || project.isEmpty()
                    || organization == null || organization.isEmpty()
                    || um == null || um.getPermission() == null || um.getPermission().get(project) == null) {
                return Mono.empty();
            } else {
                return validator
                        .execute(path, method, project, um.getPermission().get(project).get("group"))
                        .flatMap(permission -> Mono.just(exchange.mutate().request(
                                exchange.getRequest().mutate().header(USER_INFO_PERMISSION_HEADER, JsonUtil.toJson(permission)).build()
                        ).build()));
            }
        }
    }

    /**
     * User Model
     */
    @Data
    @Accessors(chain = true)
    private static class UserModel implements Serializable {
        private Map<String, Map<String, List<String>>> permission = new HashMap<>();
    }

}
