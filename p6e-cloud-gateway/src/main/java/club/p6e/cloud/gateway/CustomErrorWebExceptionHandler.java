package club.p6e.cloud.gateway;

import club.p6e.coat.common.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.CodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * 自定义全局异常处理
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
public class CustomErrorWebExceptionHandler
        extends DefaultErrorWebExceptionHandler implements Ordered {

    /**
     * 顺序
     */
    private static final int ORDER = -4000;

    /**
     * 格式化时间对象
     */
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT
            = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 注入日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomErrorWebExceptionHandler.class);

    /**
     * 常用的名称
     */
    private static final String DATE = "date";
    private static final String CODE = "code";
    private static final String ERROR = "error";
    private static final String STATUS = "status";
    private static final String MESSAGE = "message";
    private static final String TIMESTAMP = "timestamp";
    @SuppressWarnings("ALL")
    private static final String PATH = "path";
    @SuppressWarnings("ALL")
    private static final String REQUEST_ID = "requestId";

    /**
     * 构造方法初始化
     */
    public CustomErrorWebExceptionHandler(
            CodecConfigurer codecConfigurer,
            ApplicationContext applicationContext
    ) {
        super(
                new DefaultErrorAttributes(),
                new WebProperties.Resources(),
                null,
                applicationContext
        );
        this.setMessageReaders(codecConfigurer.getReaders());
        this.setMessageWriters(codecConfigurer.getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return r -> Mono.just((HandlerFunction<ServerResponse>) request -> {
            final Throwable error = errorAttributes.getError(request);
            LOGGER.error(error.getMessage());
            final Map<String, Object> errorMap =
                    this.getErrorAttributes(request, ErrorAttributeOptions.defaults());
            errorMap.put(CODE, errorMap.get(STATUS));
            errorMap.put(MESSAGE, errorMap.get(ERROR));
            final Object date = errorMap.get(TIMESTAMP);
            if (date instanceof Date) {
                errorMap.remove(TIMESTAMP);
                errorMap.put(DATE, SIMPLE_DATE_FORMAT.format(date));
            }
            errorMap.remove(ERROR);
            errorMap.remove(STATUS);
            return ServerResponse
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(JsonUtil.toJson(errorMap)), String.class);
        });
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

}









