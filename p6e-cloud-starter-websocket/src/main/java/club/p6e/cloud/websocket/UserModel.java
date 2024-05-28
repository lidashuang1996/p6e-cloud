package club.p6e.cloud.websocket;

import club.p6e.coat.websocket.User;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author lidashuang
 * @version 1.0
 */
@Data
@Accessors(chain = true)
public class UserModel implements User, Serializable {

    private String id;

    @Override
    public String id() {
        return id;
    }

}
