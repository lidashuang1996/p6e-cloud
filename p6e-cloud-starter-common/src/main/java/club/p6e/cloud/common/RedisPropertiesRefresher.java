package club.p6e.cloud.common;

import club.p6e.coat.common.utils.JsonUtil;
import jakarta.annotation.Nonnull;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author lidashuang
 * @version 1.0
 */
public abstract class RedisPropertiesRefresher {

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
     * 配置主题
     */
    private static String CONFIG_TOPIC = "p6e-cloud-config";


    private volatile long timestamp = 0;

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
    public RedisPropertiesRefresher(org.springframework.data.redis.core.StringRedisTemplate template, ConfigurableApplicationContext context) {
        this.stringRedisTemplate = template;
        context.addApplicationListener(new ReadyEventListener(this));
        context.addApplicationListener(new ContextClosedEventListener(this));
    }

    /**
     * 构造方法初始化
     *
     * @param template 模板对象
     */
    public RedisPropertiesRefresher(org.springframework.data.redis.core.ReactiveStringRedisTemplate template, ConfigurableApplicationContext context) {
        this.reactiveStringRedisTemplate = template;
        context.addApplicationListener(new ReadyEventListener(this));
        context.addApplicationListener(new ContextClosedEventListener(this));
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
        final String cmd = JsonUtil.toJson(new HashMap<>() {{
            put("type", "init");
        }});
        final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        try {
            Class.forName("org.springframework.data.redis.core.StringRedisTemplate");
            if (stringRedisTemplate != null && stringRedisTemplate.getConnectionFactory() != null) {
                new RedisMessageListenerContainer() {{
                    addMessageListener((message, pattern) -> execute(
                            JsonUtil.fromJsonToMap(new String(message.getBody(), StandardCharsets.UTF_8), String.class, String.class)
                    ), List.of(ChannelTopic.of(CONFIG_TOPIC)));
                    setConnectionFactory(stringRedisTemplate.getConnectionFactory());
                    afterPropertiesSet();
                    start();
                }};
                executor.scheduleAtFixedRate(() -> {
                    stringRedisTemplate.convertAndSend(CONFIG_TOPIC, JsonUtil.toJson(new HashMap<>() {{
                        put("type", "heartbeat");
                    }}));
                }, 5, 30, TimeUnit.SECONDS);
                return;
            }
        } catch (Exception e) {
            // ignore exception
        }
        try {
            Class.forName("org.springframework.data.redis.core.ReactiveStringRedisTemplate");
            if (reactiveStringRedisTemplate != null) {
                subscription = reactiveStringRedisTemplate
                        .listenTo(ChannelTopic.of(CONFIG_TOPIC))
                        .map(message -> JsonUtil.fromJsonToMap(message.getMessage(), String.class, String.class))
                        .subscribe(this::execute);
                reactiveStringRedisTemplate
                        .convertAndSend(CONFIG_TOPIC, cmd)
                        .publishOn(reactor.core.scheduler.Schedulers.single())
                        .subscribe();
                executor.scheduleAtFixedRate(() -> {
                    reactiveStringRedisTemplate.convertAndSend(CONFIG_TOPIC, JsonUtil.toJson(new HashMap<>() {{
                        put("type", "heartbeat");
                    }})).subscribe();
                }, 5, 30, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            // ignore exception
        }
    }

    /**
     * 关闭
     */
    protected void close() {
        try {
            if (subscription != null && subscription.isDisposed()) {
                subscription.dispose();
            }
        } catch (Exception e) {
            // ignore exception
        }
        try {
            Class.forName("org.springframework.data.redis.core.StringRedisTemplate");
            stringRedisTemplate = null;
        } catch (Exception e) {
            // ignore exception
        }
        try {
            Class.forName("org.springframework.data.redis.core.ReactiveStringRedisTemplate");
            stringRedisTemplate = null;
        } catch (Exception e) {
            // ignore exception
        }
    }

    /**
     * 执行
     */
    protected void execute(Map<String, String> message) {
        if (message != null && message.get("type") != null) {
            if ("heartbeat".equalsIgnoreCase(message.get("type"))) {
                if (timestamp <= 0) {
                    synchronized (this) {
                        if (timestamp <= 0) {
                            try {
                                Class.forName("org.springframework.data.redis.core.StringRedisTemplate");
                                stringRedisTemplate.convertAndSend(CONFIG_TOPIC, JsonUtil.toJson(new HashMap<>() {{
                                    put("type", "init");
                                }}));
                                timestamp = System.currentTimeMillis();
                                return;
                            } catch (Exception e) {
                                // ignore exception
                            }
                            try {
                                Class.forName("org.springframework.data.redis.core.ReactiveStringRedisTemplate");
                                reactiveStringRedisTemplate.convertAndSend(CONFIG_TOPIC, JsonUtil.toJson(new HashMap<>() {{
                                    put("type", "init");
                                }})).subscribe();
                                timestamp = System.currentTimeMillis();
                            } catch (Exception e) {
                                // ignore exception
                            }
                        }
                    }
                }
            } else if ("config".equalsIgnoreCase(message.get("type"))
                    && message.get("format") != null && message.get("content") != null) {
                config(message.get("format"), message.get("content"));
            }
        }
    }

    /**
     * 执行
     */
    protected abstract void config(String format, String content);

}
