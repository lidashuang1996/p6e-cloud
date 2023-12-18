package club.p6e.cloud.file.cache;

import reactor.core.publisher.Mono;

/**
 * @author lidashuang
 * @version 1.0
 */
public interface VoucherCache {

    /**
     * 过期时间
     */
    long EXPIRATION_TIME = 3600;

    /**
     * VOUCHER 缓存前缀
     */
    String VOUCHER_PREFIX = "FILE:VOUCHER:";

    /**
     * 写入凭证内容
     *
     * @param voucher 凭证
     * @param content 内容
     */
    Mono<String> set(String voucher, String content);

    /**
     * 读取凭证
     *
     * @param voucher 凭证
     * @return 凭证内容
     */
    Mono<String> get(String voucher);

    /**
     * 删除凭证
     *
     * @param voucher 凭证
     */
    Mono<String> del(String voucher);

}
