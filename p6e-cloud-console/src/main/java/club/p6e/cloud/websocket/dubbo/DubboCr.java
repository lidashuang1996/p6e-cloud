package club.p6e.cloud.websocket.dubbo;

import club.p6e.cloud.websocket.api.PushService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class DubboCr {

    /**
     * Web Socket Main 对象
     */
    @DubboReference
    private PushService service;

    public void send() {
        service.push(List.of("123456"), "DEFAULT", "123", "123", "213");
    }


}