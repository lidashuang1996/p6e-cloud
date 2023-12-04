package club.p6e.cloud.websocket.api;

import java.io.Serializable;
import java.util.List;

/**
 * Web Socket Push Service API
 *
 * @author lidashuang
 * @version 1.0
 */
public interface PushService extends Serializable {

    /**
     * 发送消息
     *
     * @param recipients 接收者列表
     * @param name       通道名称
     * @param bytes      字节数据
     */
    public void push(List<String> recipients, String name, byte[] bytes);

    /**
     * 发送消息
     *
     * @param recipients 接收者列表
     * @param name       通道名称
     * @param id         消息编号
     * @param type       消息类型
     * @param content    消息内容
     */
    public void push(List<String> recipients, String name, String id, String type, String content);

}
