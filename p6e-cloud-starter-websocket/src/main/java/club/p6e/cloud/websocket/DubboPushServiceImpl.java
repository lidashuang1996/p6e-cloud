package club.p6e.cloud.websocket;

import club.p6e.cloud.websocket.api.PushService;
import club.p6e.coat.websocket.WebSocketMain;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author lidashuang
 * @version 1.0
 */
@Component
@DubboService
public class DubboPushServiceImpl implements PushService {

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
    public DubboPushServiceImpl(WebSocketMain wsm) {
        this.wsm = wsm;
    }

    @Override
    public void push(List<String> recipients, String name, byte[] bytes) {
        wsm.push(u -> recipients.contains(u.id()), name, bytes);
    }

    @Override
    public void push(List<String> recipients, String name, String id, String type, String content) {
        wsm.push(u -> recipients.contains(u.id()), name, id, type, content);
    }
}