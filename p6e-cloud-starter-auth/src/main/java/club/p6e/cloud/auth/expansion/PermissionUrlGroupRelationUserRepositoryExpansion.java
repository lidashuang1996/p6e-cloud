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
public class PermissionUrlGroupRelationUserRepositoryExpansion {

    /**
     * 模板对象
     */
    private final R2dbcEntityTemplate template;

    /**
     * 构造方法初始化
     *
     * @param template 模板对象
     */
    public PermissionUrlGroupRelationUserRepositoryExpansion(R2dbcEntityTemplate template) {
        this.template = template;
    }

    /**
     * 根据 ID 查询数据
     *
     * @param id ID
     * @return Mono/PermissionUrlGroupRelationUserModelExpansion
     */
    public Flux<PermissionUrlGroupRelationUserModelExpansion> findByUidList(Integer id) {
        return template.select(
                Query.query(Criteria.where(PermissionUrlGroupRelationUserModelExpansion.UID).is(id)),
                PermissionUrlGroupRelationUserModelExpansion.class
        );
    }

}
