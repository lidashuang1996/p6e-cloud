package club.p6e.cloud.file.aspect;

import club.p6e.coat.common.error.AuthException;
import club.p6e.coat.file.aspect.SliceUploadAspect;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Auth Slice Upload Aspect Impl
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
public class AuthSliceUploadAspectImpl implements SliceUploadAspect {

    /**
     * AuthValidator object
     */
    private final AuthValidator validator;

    /**
     * Constructor initializers
     *
     * @param validator AuthValidator object
     */
    public AuthSliceUploadAspectImpl(AuthValidator validator) {
        this.validator = validator;
    }

    @Override
    public int order() {
        return -1000;
    }

    @Override
    public Mono<Boolean> before(Map<String, Object> data) {
        return validator
                .execute(data)
                .map(s -> true)
                .switchIfEmpty(Mono.error(new AuthException(
                        this.getClass(),
                        "fun before(Map<String, Object> data). ==> " +
                                "before(...) request for authentication information does not exist or has expired.",
                        "before(...) request for authentication information does not exist or has expired."
                )));
    }

    @Override
    public Mono<Boolean> after(Map<String, Object> data, Map<String, Object> result) {
        return Mono.just(true);
    }

}
