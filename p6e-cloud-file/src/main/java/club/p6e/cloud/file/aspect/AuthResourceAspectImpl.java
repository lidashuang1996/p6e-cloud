package club.p6e.cloud.file.aspect;

import club.p6e.coat.file.aspect.DefaultResourceAspectImpl;
import club.p6e.coat.file.aspect.ResourceAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 资源查看操作的切面（钩子）的默认实现
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = AuthResourceAspectImpl.class,
        ignored = AuthResourceAspectImpl.class
)
public class AuthResourceAspectImpl
        extends DefaultResourceAspectImpl implements ResourceAspect {

    private final AuthValidator validator;

    public AuthResourceAspectImpl(AuthValidator validator) {
        this.validator = validator;
    }

    @Override
    public Mono<Boolean> before(Map<String, Object> data) {
        return validator
                .execute(data)
                .map(s -> true)
                .switchIfEmpty(Mono.just(false));
    }

    @Override
    public Mono<Boolean> after(Map<String, Object> data) {
        return super.after(data);
    }

}
