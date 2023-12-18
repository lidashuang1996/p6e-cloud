package club.p6e.cloud.file.controller;

import club.p6e.coat.common.utils.JsonUtil;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义全局异常处理
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
public class ExceptionConfigController implements ErrorWebExceptionHandler, Ordered {

    /**
     * 注入日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionConfigController.class);

    /**
     * 格式化时间对象
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Nonnull
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable throwable) {
        LOGGER.error(throwable.getMessage());
        final Map<String, Object> result = new HashMap<>();
        final ServerHttpRequest request = exchange.getRequest();
        final ServerHttpResponse response = exchange.getResponse();
        result.put("path", request.getPath().value());
        result.put("requestId", request.getId());
        result.put("message", throwable.getMessage());
        result.put("date", LocalDateTime.now().format(DATE_TIME_FORMATTER));
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        if (throwable instanceof final ResponseStatusException responseStatusException) {
            result.put("code", responseStatusException.getStatusCode().value());
            exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(
                    JsonUtil.toJson(result).getBytes(StandardCharsets.UTF_8))));
        }
        result.put("code", "500");
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(
                JsonUtil.toJson(result).getBytes(StandardCharsets.UTF_8))));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
