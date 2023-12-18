package club.p6e.cloud.websocket.controller;

import club.p6e.coat.common.utils.JsonUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义处理异常
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class ExceptionConfigController extends BasicErrorController {

    /**
     * 注入的日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionConfigController.class);

    /**
     * 格式化时间对象
     */
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @SuppressWarnings("ALL")
    @Value("${server.error.path:${error.path:/error}}")
    private String path;

    /**
     * 自定义处理异常
     *
     * @param server 实现父类的方法
     */
    public ExceptionConfigController(ServerProperties server) {
        super(new DefaultErrorAttributes(), server.getError());
    }

    /**
     * 对异常的基础处理
     *
     * @param request HttpServletRequest 请求对象
     * @return 异常结果数据对象
     */
    private Map<String, Object> processor(HttpServletRequest request) {
        final Map<String, Object> result = new HashMap<>(16);
        final Map<String, Object> attributes = getErrorAttributes(request, ErrorAttributeOptions.defaults());
        result.put("path", attributes.get("path"));
        result.put("code", attributes.get("status"));
        result.put("message", attributes.get("error"));
        final Object date = attributes.get("timestamp");
        if (date instanceof Date) {
            attributes.remove("timestamp");
            result.put("date", SIMPLE_DATE_FORMAT.format(date));
        }
        return result;
    }

    @Override
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        final HttpStatus status = this.getStatus(request);
        return new ResponseEntity<>(processor(request), status);
    }

    @Override
    public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.getWriter().write(JsonUtil.toJson(processor(request)));
            response.getWriter().flush();
            response.getWriter().close();
        } catch (Exception e) {
            LOGGER.error("EXCEPTION", e);
        }
        return null;
    }
}
