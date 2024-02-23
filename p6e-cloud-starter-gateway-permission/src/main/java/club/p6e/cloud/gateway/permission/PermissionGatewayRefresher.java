package club.p6e.cloud.gateway.permission;

import club.p6e.coat.permission.PermissionTask;
import club.p6e.coat.permission.PermissionTaskActuator;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

/**
 * 权限网关配置刷新器
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
public class PermissionGatewayRefresher implements PermissionTask {

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
                .subscribe();
    }

    /**
     * 执行
     */
    protected long execute() {
        this.actuator.execute();
        return System.currentTimeMillis();
    }

    @Override
    public long version() {
        return 3600L;
    }

    @Override
    public long interval() {
        return (long) Math.floor(System.currentTimeMillis() / 1000D);
    }
}
