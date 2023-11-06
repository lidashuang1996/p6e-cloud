package club.p6e.cloud.file.aspect;

import club.p6e.cloud.file.cache.VoucherCache;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = AuthValidator.class,
        ignored = AuthValidator.class
)
public class AuthValidator {

    private final VoucherCache cache;

    public AuthValidator(VoucherCache cache) {
        this.cache = cache;
    }

    public Mono<String> execute(Map<String, Object> data) {
        if (data == null
                || data.get("v") instanceof String
                || data.get("voucher") instanceof String) {
            return Mono.empty();
        } else {
            String voucher = String.valueOf(data.get("v"));
            if (voucher == null) {
                voucher = String.valueOf(data.get("voucher"));
            }
            if (voucher == null) {
                return Mono.empty();
            } else {
                return cache
                        .get(voucher)
                        .map(u -> {
                            data.put("operator", u);
                            return u;
                        });
            }
        }
    }

}
