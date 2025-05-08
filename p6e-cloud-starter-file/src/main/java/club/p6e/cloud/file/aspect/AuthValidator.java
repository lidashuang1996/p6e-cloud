package club.p6e.cloud.file.aspect;

import club.p6e.cloud.file.Properties;
import club.p6e.coat.common.utils.AesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;

/**
 * Auth Validator
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = AuthValidator.class,
        ignored = AuthValidator.class
)
public class AuthValidator {

    /**
     * 注入日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthValidator.class);

    /**
     * 配置文件对象
     */
    private final Properties properties;

    /**
     * 构造方法初始化
     *
     * @param properties 配置文件对象
     */
    public AuthValidator(Properties properties) {
        this.properties = properties;
    }

    /**
     * 执行验证
     *
     * @param data 请求参数数据
     * @return 凭证包含的数据
     */
    public Mono<String> execute(Map<String, Object> data) {
        String voucher = String.valueOf(data.get("v"));
        if (voucher != null) {
            voucher = voucher.startsWith("[") ? voucher.substring(1) : voucher;
            voucher = voucher.endsWith("]") ? voucher.substring(0, voucher.length() - 1) : voucher;
        }
        if (voucher == null || voucher.isEmpty()) {
            voucher = String.valueOf(data.get("voucher"));
        }
        if (voucher != null) {
            voucher = voucher.startsWith("[") ? voucher.substring(1) : voucher;
            voucher = voucher.endsWith("]") ? voucher.substring(0, voucher.length() - 1) : voucher;
        }
        if (voucher != null && !voucher.isEmpty()) {
            voucher = voucher.split(",")[0];
            try {
                final String secret = properties.getSecret();
                final String content = new String(AesUtil.decryption(
                        HexFormat.of().parseHex(voucher), AesUtil.stringToKey(secret)
                ), StandardCharsets.UTF_8);
                final int firstIndex = content.indexOf("@");
                final int lastIndex = content.lastIndexOf("@");
                if (firstIndex > 0 && lastIndex > firstIndex) {
                    final String timestamp = content.substring((lastIndex + 1));
                    if ((Long.parseLong(timestamp) + 7200L) > (System.currentTimeMillis() / 1000L)) {
                        final String id = content.substring(0, firstIndex);
                        final String node = content.substring(firstIndex + 1, lastIndex);
                        data.put("$id", id);
                        data.put("$node", node);
                        data.put("$operator", id);
                        return Mono.just(content);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("[ AUTH VALIDATOR ] ERROR >>> {}", e.getMessage());
            }
        }
        return Mono.empty();
    }

}
