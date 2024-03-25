package club.p6e.cloud.auth.expansion;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnProperty(name = "p6e.cloud.auth.permission.enabled", havingValue = "true")
public class PermissionUrlGroupRepositoryExpansion {

    /**
     * 模板对象
     */
    private final R2dbcEntityTemplate template;

    /**
     * 构造方法初始化
     *
     * @param template 模板对象
     */
    public PermissionUrlGroupRepositoryExpansion(R2dbcEntityTemplate template) {
        this.template = template;
    }

    /**
     * 根据 ID 查询数据
     *
     * @param id ID
     * @return Mono/PermissionUrlGroupModelExpansion 用户模型对象
     */
    public Mono<PermissionUrlGroupModelExpansion> findById(Integer id) {
        return template.selectOne(
                Query.query(Criteria.where(PermissionUrlGroupModelExpansion.ID).is(id)),
                PermissionUrlGroupModelExpansion.class
        );
    }

    /**
     * 根据 PID 查询数据
     *
     * @param pid PID
     * @return Flux/PermissionUrlGroupModelExpansion 用户模型对象
     */
    public Flux<PermissionUrlGroupModelExpansion> findByPid(Integer pid) {
        return template.select(
                Query.query(Criteria.where(PermissionUrlGroupModelExpansion.PARENT).is(pid)),
                PermissionUrlGroupModelExpansion.class
        );
    }

}
