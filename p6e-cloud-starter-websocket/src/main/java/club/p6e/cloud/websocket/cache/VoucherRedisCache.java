package club.p6e.cloud.websocket.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnClass(name = "org.springframework.web.reactive.package-info")
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
    public Mono<String> del(String voucher) {
        return template.delete(VOUCHER_PREFIX + voucher).map(l -> voucher);
    }

    @Override
    public Mono<String> get(String voucher) {
        return template.opsForValue().get(VOUCHER_PREFIX + voucher);
    }

    @Override
    public Mono<String> set(String voucher, String content) {
        return template
                .opsForValue()
                .set(VOUCHER_PREFIX + voucher, content, Duration.ofSeconds(EXPIRATION_TIME))
                .flatMap(b -> b ? Mono.just(voucher) : Mono.empty());
    }

}
