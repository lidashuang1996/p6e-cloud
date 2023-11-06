package club.p6e.cloud.file.aspect;

import club.p6e.coat.file.aspect.DefaultOpenUploadAspectImpl;
import club.p6e.coat.file.aspect.OpenUploadAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 打开上传操作的切面（钩子）的默认实现
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = AuthOpenUploadAspectImpl.class,
        ignored = AuthOpenUploadAspectImpl.class
)
public class AuthOpenUploadAspectImpl
        extends DefaultOpenUploadAspectImpl implements OpenUploadAspect {

    private final AuthValidator validator;

    public AuthOpenUploadAspectImpl(AuthValidator validator) {
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
