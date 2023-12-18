package club.p6e.cloud.websocket;

import club.p6e.coat.websocket.User;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author lidashuang
 * @version 1.0
 */
@Data
@Accessors(chain = true)
public class UserModel implements User, Serializable {

    private Integer id;
    private Integer status;
    private Integer enabled;
    private String account;
    private String phone;
    private String mailbox;
    private String name;
    private String nickname;
    private String avatar;
    private String description;

    /**
     * 用户所在的项目/组/群/房间/频道
     */
    private final List<String> extend;

    @Override
    public String id() {
        return String.valueOf(id);
    }


}
