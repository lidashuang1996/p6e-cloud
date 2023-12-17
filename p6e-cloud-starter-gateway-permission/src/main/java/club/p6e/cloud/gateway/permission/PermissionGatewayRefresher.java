package club.p6e.cloud.gateway.permission;

import club.p6e.coat.permission.PermissionTaskActuator;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Schedulers;

/**
 * @author lidashuang
 * @version 1.0
 */
@Component
public class PermissionGatewayRefresher {

    /**
     * 配置主题
     */
    private static String CONFIG_TOPIC = "p6e-cloud-gateway-permission-topic";

    /**
     * 权限任务执行器
     */
    private final PermissionTaskActuator actuator;

    /**
     * 设置配置主题
     *
     * @param topic 主题名称
     */
    public static void setConfigTopic(String topic) {
        CONFIG_TOPIC = topic;
    }

    /**
     * 构造方法初始化
     *
     * @param actuator 权限任务执行器
     * @param template Redis String Template 模板对象
     */
    public PermissionGatewayRefresher(PermissionTaskActuator actuator, ReactiveStringRedisTemplate template) {
        this.actuator = actuator;
        template
                .listenTo(ChannelTopic.of(CONFIG_TOPIC))
                .map(m -> execute())
                .publishOn(Schedulers.single())
                .subscribe();
    }

    /**
     * 执行
     */
    protected long execute() {
        actuator.execute();
        return System.currentTimeMillis();
    }

}
