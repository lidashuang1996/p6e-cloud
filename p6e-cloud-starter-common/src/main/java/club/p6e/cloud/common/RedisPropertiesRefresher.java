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
import reactor.core.Disposable;

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
    private static String CONFIG_TOPIC = "p6e-cloud-file-config-topic";

    /**
     * Disposable 对象
     */
    private Disposable subscription;

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
//        this.subscription = this.template
//                .listenTo(ChannelTopic.of(CONFIG_TOPIC))
//                .map(message -> {
//                    try {
//                        return JsonUtil.fromJson(message.getMessage(), MessageModel.class);
//                    } catch (Exception e) {
//                        return new MessageModel("error", "");
//                    }
//                })
//                .publishOn(Schedulers.single())
//                .subscribe(this::execute);
//        this.template
//                .convertAndSend(CONFIG_TOPIC, JsonUtil.toJson(new MessageModel("init", "")))
//                .publishOn(Schedulers.single())
//                .subscribe();
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
