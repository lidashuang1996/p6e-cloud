package club.p6e.cloud.websocket.controller;

import club.p6e.cloud.websocket.UserModel;
import club.p6e.coat.common.context.ResultContext;
import club.p6e.coat.common.error.ParameterException;
import club.p6e.coat.common.utils.GeneratorUtil;
import club.p6e.coat.websocket.WebSocketMain;
import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 自定义接口
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@RestController
@RequestMapping("/push")
public class PushController {

    /**
     * 推送参数对象
     */
    @Data
    public static class PushParam implements Serializable {
        private String name;
        private String type;
        private String content;
        private List<String> data;
    }

    /**
     * 时间格式化对象
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * Web Socket Main 对象
     */
    private final WebSocketMain wsm;

    /**
     * 构造方法初始化
     *
     * @param wsm Web Socket Main 对象
     */
    @SuppressWarnings("ALL")
    public PushController(WebSocketMain wsm) {
        this.wsm = wsm;
    }

    @PostMapping("/all")
    public ResultContext pushAll(@RequestBody PushParam param) {
        if (param == null
                || param.getType() == null
                || param.getContent() == null) {
            throw new ParameterException(
                    this.getClass(),
                    "fun pushAll(PushParam param).",
                    "request parameter exception, please check your network request."
            );
        }
        final String id = DATE_TIME_FORMATTER.format(LocalDateTime.now()) + GeneratorUtil.uuid();
        final String name = param.getName() == null ? "DEFAULT" : param.getName();
        final String type = param.getType();
        final String content = param.getContent();
        wsm.push(u -> true, name, id, type, content);
        return ResultContext.build(id);
    }


    @PostMapping("/data")
    public ResultContext pushData(@RequestBody PushParam param) {
        if (param == null
                || param.getType() == null
                || param.getContent() == null
                || param.getData() == null
                || param.getData().isEmpty()) {
            throw new ParameterException(
                    this.getClass(),
                    "fun pushData(PushParam param).",
                    "request parameter exception, please check your network request."
            );
        }
        final String id = DATE_TIME_FORMATTER.format(LocalDateTime.now()) + GeneratorUtil.uuid();
        final String name = param.getName() == null ? "DEFAULT" : param.getName();
        final String type = param.getType();
        final String content = param.getContent();
        final List<String> data = param.getData();
        wsm.push(u -> {
            if (u instanceof final UserModel cu && cu.getExtend() != null) {
                for (final String item : data) {
                    if (cu.getExtend().contains(item)) {
                        return true;
                    }
                }
            }
            return false;
        }, name, id, type, content);
        return ResultContext.build(id);
    }

}
