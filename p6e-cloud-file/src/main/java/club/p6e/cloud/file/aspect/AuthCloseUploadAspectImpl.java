package club.p6e.cloud.file.aspect;

import club.p6e.coat.file.aspect.CloseUploadAspect;
import club.p6e.coat.file.aspect.DefaultCloseUploadAspectImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 切面（钩子）的默认实现
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = AuthCloseUploadAspectImpl.class,
        ignored = AuthCloseUploadAspectImpl.class
)
public class AuthCloseUploadAspectImpl
        extends DefaultCloseUploadAspectImpl implements CloseUploadAspect {

    private final AuthValidator validator;

    public AuthCloseUploadAspectImpl(AuthValidator validator) {
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
    public Mono<Boolean> after(Map<String, Object> data, Map<String, Object> result) {
        return super.after(data, result);
    }

}
