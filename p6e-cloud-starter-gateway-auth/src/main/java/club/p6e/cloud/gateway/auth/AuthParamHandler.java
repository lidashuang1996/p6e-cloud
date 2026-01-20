package club.p6e.cloud.gateway.auth;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface AuthParamHandler {

    Mono<Map<String, Object>> execute(ServerWebExchange exchange);

}
