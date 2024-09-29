package club.p6e.cloud.file.aspect;

import club.p6e.cloud.file.Properties;
import club.p6e.coat.common.utils.AesUtil;
import club.p6e.coat.common.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        if (voucher == null) {
            voucher = String.valueOf(data.get("voucher"));
        }
        if (voucher != null) {
            try {
                final String secret = properties.getSecret();
                final String content = AesUtil.decryption(voucher, AesUtil.stringToKey(secret));
                final int index = content.lastIndexOf("@");
                if (index > 0) {
                    final String timestamp = content.substring((index + 1));
                    if ((Long.parseLong(timestamp) + 30) > (System.currentTimeMillis() / 1000L)) {
                        final Map<String, Object> cm = JsonUtil.fromJsonToMap(content, String.class, Object.class);
                        if (cm != null && cm.get("id") != null && cm.get("node") != null) {
                            final String id = String.valueOf(cm.get("id"));
                            final String node = String.valueOf(cm.get("node"));
                            data.put("$id", id);
                            data.put("$node", node);
                            data.put("$operator", id);
                            return Mono.just(content);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("[ AUTH VALIDATOR ] ERROR >>> {}", e.getMessage());
            }
        }
        return Mono.empty();
    }
}
