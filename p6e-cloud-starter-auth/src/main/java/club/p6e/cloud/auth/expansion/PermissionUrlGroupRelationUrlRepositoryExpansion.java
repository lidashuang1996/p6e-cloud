package club.p6e.cloud.auth.expansion;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * @author lidashuang
 * @version 1.0
 */
@Primary
@Component
@ConditionalOnProperty(name = "p6e.cloud.auth.permission.enabled", havingValue = "true")
public class PermissionUrlGroupRelationUrlRepositoryExpansion {

    /**
     * 模板对象
     */
    private final R2dbcEntityTemplate template;

    /**
     * 构造方法初始化
     *
     * @param template 模板对象
     */
    public PermissionUrlGroupRelationUrlRepositoryExpansion(R2dbcEntityTemplate template) {
        this.template = template;
    }

    /**
     * 根据 UID 查询数据
     *
     * @param gid GID
     * @return Flux/PermissionUrlGroupRelationUrlModelExpansion
     */
    public Flux<PermissionUrlGroupRelationUrlModelExpansion> findByGidList(Integer gid) {
        return template.select(
                Query.query(Criteria.where(PermissionUrlGroupRelationUrlModelExpansion.GID).is(gid)),
                PermissionUrlGroupRelationUrlModelExpansion.class
        );
    }

}
