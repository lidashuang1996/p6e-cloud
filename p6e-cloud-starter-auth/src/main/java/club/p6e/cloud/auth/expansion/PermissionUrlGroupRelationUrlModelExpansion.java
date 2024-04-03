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
@Table(PermissionUrlGroupRelationUrlModelExpansion.TABLE)
public class PermissionUrlGroupRelationUrlModelExpansion implements Serializable {

    public static final String TABLE = "p6e_permission_url_group_relation_url";

    public static final String GID = "gid";
    public static final String UID = "uid";
    public static final String CONFIG = "config";
    public static final String ATTRIBUTE = "attribute";

    private Integer gid;
    private Integer uid;
    private String config;
    private String attribute;

}
