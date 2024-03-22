package club.p6e.cloud.auth.expansion;

import club.p6e.coat.auth.model.UserModel;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户模型
 *
 * @author lidashuang
 * @version 1.0
 */
@Data
@Accessors(chain = true)
@Table(PermissionUrlGroupModelExpansion.TABLE)
public class PermissionUrlGroupModelExpansion implements Serializable {

    public static final String TABLE = "p6e_permission_url_group";

    public static final String ID = "id";
    public static final String MARK = "mark";
    public static final String WEIGHT = "weight";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String CREATE_DATE = "createDate";
    public static final String UPDATE_DATE = "updateDate";
    public static final String OPERATOR = "operator";
    public static final String VERSION = "version";
    public static final String PARENT = "parent";

    @Id
    private Integer id;
    private Integer parent;
    private String mark;
    private Integer weight;
    private String name;
    private String description;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private String operator;
    private Integer version;

}
