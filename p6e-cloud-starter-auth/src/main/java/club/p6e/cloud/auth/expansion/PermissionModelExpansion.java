package club.p6e.cloud.auth.expansion;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author lidashuang
 * @version 1.0
 */
@Data
@Accessors(chain = true)
public class PermissionModelExpansion implements Serializable {

    private Integer gid;
    private Integer uid;
    private String config;
    private String attribute;
    private Integer parent;
    private String mark;
    private Integer weight;
    private String name;

}
