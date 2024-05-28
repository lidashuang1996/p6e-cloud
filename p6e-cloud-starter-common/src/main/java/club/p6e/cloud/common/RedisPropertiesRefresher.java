package club.p6e.cloud.common;

import club.p6e.coat.common.utils.JsonUtil;
import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.io.Serializable;
import java.util.Map;

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
    private static String CONFIG_TOPIC = "p6e-cloud-config-topic";

    /**
     * Disposable 对象
     */
    private reactor.core.Disposable subscription;

    private org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;
    /**
     * 模板对象
     */
    private org.springframework.data.redis.core.ReactiveStringRedisTemplate reactiveStringRedisTemplate;

    /**
     * 构造方法初始化
     *
     * @param template 模板对象
     */
    public RedisPropertiesRefresher(org.springframework.data.redis.core.StringRedisTemplate template) {
        this.stringRedisTemplate = template;
    }

    /**
     * 构造方法初始化
     *
     * @param template 模板对象
     */
    public RedisPropertiesRefresher(org.springframework.data.redis.core.ReactiveStringRedisTemplate template) {
        this.reactiveStringRedisTemplate = template;
    }

    /**
     * 设置配置主题
     *
     * @param topic 主题名称
     */
    public static void setConfigTopic(String topic) {
        CONFIG_TOPIC = topic;
    }

    /**
     * 初始化
     */
    protected void init() {
        if (stringRedisTemplate != null
                && stringRedisTemplate.getConnectionFactory() != null) {
            final RedisMessageListenerContainer container = new RedisMessageListenerContainer();
            container.setConnectionFactory(stringRedisTemplate.getConnectionFactory());
            container.addMessageListener((message, pattern) -> {
                try {
                    execute(JsonUtil.fromJson(new String(message.getBody()), MessageModel.class));
                } catch (Exception e) {
                    execute(new MessageModel("error", ""));
                }
            }, new ChannelTopic(CONFIG_TOPIC));
            stringRedisTemplate.convertAndSend(CONFIG_TOPIC, JsonUtil.toJson(new MessageModel("init", "")));
        }
        if (reactiveStringRedisTemplate != null) {
            subscription = reactiveStringRedisTemplate
                    .listenTo(ChannelTopic.of(CONFIG_TOPIC))
                    .map(message -> {
                        try {
                            return JsonUtil.fromJson(message.getMessage(), MessageModel.class);
                        } catch (Exception e) {
                            return new MessageModel("error", "");
                        }
                    })
                    .publishOn(reactor.core.scheduler.Schedulers.single())
                    .subscribe(this::execute);
            reactiveStringRedisTemplate
                    .convertAndSend(CONFIG_TOPIC, JsonUtil.toJson(new MessageModel("init", "")))
                    .publishOn(reactor.core.scheduler.Schedulers.single())
                    .subscribe();
        }
    }

    /**
     * 关闭
     */
    protected void close() {
        if (stringRedisTemplate != null) {
            stringRedisTemplate = null;
        }
        if (reactiveStringRedisTemplate != null) {
            if (subscription != null && subscription.isDisposed()) {
                subscription.dispose();
            }
            reactiveStringRedisTemplate = null;
        }
    }

    /**
     * 执行
     */
    protected void execute(MessageModel message) {
        if (message != null
                && message.getData() != null
                && "config".equalsIgnoreCase(message.getType())) {
            final String content = message.getData();
            try {
                final Map<String, String> config = JsonUtil.fromJsonToMap(content, String.class, String.class);
                if (config != null
                        && config.get("format") != null
                        && config.get("content") != null) {
                    config(config.get("format"), config.get("content"));
                }
            } catch (Exception e) {
                // ignore
            }
        }
    }

    /**
     * 执行
     */
    protected void config(String format, String content) {
    }

}
