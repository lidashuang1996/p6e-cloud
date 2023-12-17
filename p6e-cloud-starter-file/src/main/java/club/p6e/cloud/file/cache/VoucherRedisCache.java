package club.p6e.cloud.file.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = VoucherCache.class,
        ignored = VoucherRedisCache.class
)
public class VoucherRedisCache implements VoucherCache {

    /**
     * 缓存对象
     */
    private final ReactiveStringRedisTemplate template;

    /**
     * 构造方法初始化
     *
     * @param template 缓存对象
     */
    public VoucherRedisCache(ReactiveStringRedisTemplate template) {
        this.template = template;
    }

    @Override
    public Mono<String> set(String voucher, String content) {
        final String key = VOUCHER_PREFIX + voucher;
        return template
                .opsForValue()
                .set(key, content, Duration.ofSeconds(EXPIRATION_TIME))
                .flatMap(b -> b ? Mono.just(key) : Mono.empty());
    }

    @Override
    public Mono<String> get(String voucher) {
        return template.opsForValue().get(VOUCHER_PREFIX + voucher);
    }

    @Override
    public Mono<String> del(String voucher) {
        final String key = VOUCHER_PREFIX + voucher;
        return template
                .opsForValue()
                .delete(key)
                .flatMap(b -> b ? Mono.just(key) : Mono.empty());
    }

}
