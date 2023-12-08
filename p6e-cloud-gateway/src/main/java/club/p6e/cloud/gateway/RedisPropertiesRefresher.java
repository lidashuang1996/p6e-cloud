package club.p6e.cloud.gateway;

import club.p6e.coat.common.utils.JsonUtil;
import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import reactor.core.Disposable;
import reactor.core.scheduler.Schedulers;

import java.io.Serializable;

/**
 * @author lidashuang
 * @version 1.0
 */
public class RedisPropertiesRefresher {

    /**
     * Spring Init Event Listener
     */
    public static class ReadyEventListener implements ApplicationListener<ApplicationReadyEvent> {

        private final RedisPropertiesRefresher refresher;

        public ReadyEventListener(RedisPropertiesRefresher refresher) {
            this.refresher = refresher;
        }

        @Override
        public void onApplicationEvent(@Nonnull ApplicationReadyEvent event) {
            refresher.init();
        }
    }

    /**
     * Spring Close Event Listener
     */
    public static class ContextClosedEventListener implements ApplicationListener<ContextClosedEvent> {

        private final RedisPropertiesRefresher refresher;

        public ContextClosedEventListener(RedisPropertiesRefresher refresher) {
            this.refresher = refresher;
        }

        @Override
        public void onApplicationEvent(@Nonnull ContextClosedEvent event) {
            refresher.close();
        }
    }

    /**
     * inject Spring Init Event Listener Bean
     */
    @Bean
    public ReadyEventListener injectReadyEventListener(RedisPropertiesRefresher refresher) {
        return new ReadyEventListener(refresher);
    }

    /**
     * inject Spring Init Event Listener Bean
     */
    @Bean
    public ContextClosedEventListener injectContextClosedEventListener(RedisPropertiesRefresher refresher) {
        return new ContextClosedEventListener(refresher);
    }

    @Data
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class MessageModel implements Serializable {
        private String type;
        private String data;
    }

    /**
     * 配置主题
     */
    private static final String CONFIG_TOPIC = "p6e-cloud-gateway-config-topic";

    /**
     * Disposable 对象
     */
    private Disposable subscription;

    /**
     * Properties Refresher 对象
     */
    private final PropertiesRefresher refresher;

    /**
     * Redis String Template 模板对象
     */
    private final ReactiveStringRedisTemplate template;

    /**
     * 构造方法初始化
     *
     * @param refresher Properties Refresher 对象
     * @param template  Redis String Template 模板对象
     */
    public RedisPropertiesRefresher(PropertiesRefresher refresher, ReactiveStringRedisTemplate template) {
        this.template = template;
        this.refresher = refresher;
    }

    /**
     * 初始化
     */
    protected void init() {
        this.subscription = this.template
                .listenTo(ChannelTopic.of(CONFIG_TOPIC))
                .map(message -> {
                    try {
                        return JsonUtil.fromJson(message.getMessage(), MessageModel.class);
                    } catch (Exception e) {
                        return new MessageModel("error", "");
                    }
                })
                .publishOn(Schedulers.single())
                .subscribe(this::execute);
        this.template
                .convertAndSend(CONFIG_TOPIC, JsonUtil.toJson(new MessageModel("init", "")))
                .publishOn(Schedulers.single())
                .subscribe();
    }

    /**
     * 关闭
     */
    protected void close() {
        if (subscription != null && subscription.isDisposed()) {
            subscription.dispose();
        }
    }

    /**
     * 执行
     */
    protected void execute(MessageModel message) {
        if (message != null
                && "config".equalsIgnoreCase(message.getType())) {
            this.refresher.execute(JsonUtil.fromJsonToMap(message.getData(), String.class, String.class));
        }
    }

}
