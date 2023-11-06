package club.p6e.cloud.file.aspect;

import club.p6e.coat.file.aspect.DefaultSliceUploadAspectImpl;
import club.p6e.coat.file.aspect.SliceUploadAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 简单（小文件）上传操作的切面（钩子）的默认实现
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = AuthSliceUploadAspectImpl.class,
        ignored = AuthSliceUploadAspectImpl.class
)
public class AuthSliceUploadAspectImpl
        extends DefaultSliceUploadAspectImpl implements SliceUploadAspect {

    private final AuthValidator validator;

    public AuthSliceUploadAspectImpl(AuthValidator validator) {
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
