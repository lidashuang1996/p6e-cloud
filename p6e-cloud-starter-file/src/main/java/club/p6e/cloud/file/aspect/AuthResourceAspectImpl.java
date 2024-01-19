package club.p6e.cloud.file.aspect;

import club.p6e.coat.file.aspect.ResourceAspect;
import club.p6e.coat.file.error.AuthException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * @author lidashuang
 * @version 1.0
 */
@Component
public class AuthResourceAspectImpl implements ResourceAspect {

    /**
     * 认证验证器对象
     */
    private final AuthValidator validator;

    /**
     * 构造方法初始化
     *
     * @param validator 认证验证器对象
     */
    public AuthResourceAspectImpl(AuthValidator validator) {
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
                        "fun before(Map<String, Object> data).",
                        "Request authentication information has expired"
                )));
    }

    @Override
    public Mono<Boolean> after(Map<String, Object> data) {
        return Mono.just(true);
    }

}
