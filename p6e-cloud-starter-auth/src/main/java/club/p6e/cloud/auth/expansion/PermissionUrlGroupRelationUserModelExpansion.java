package club.p6e.cloud.auth.expansion;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

/**
 * 用户模型
 *
 * @author lidashuang
 * @version 1.0
 */
@Data
@Accessors(chain = true)
@Table(PermissionUrlGroupRelationUserModelExpansion.TABLE)
public class PermissionUrlGroupRelationUserModelExpansion implements Serializable {

    public static final String TABLE = "p6e_permission_url_group_relation_user";

    public static final String UID = "uid";
    public static final String GID = "gid";

    private Integer uid;
    private Integer gid;

}
