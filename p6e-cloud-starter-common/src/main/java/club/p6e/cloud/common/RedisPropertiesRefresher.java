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
import java.util.concurrent.atomic.AtomicLong;

/**
 * Redis Properties Refresher
 *
 * @author lidashuang
 * @version 1.0
 */
@SuppressWarnings("ALL")
public abstract class RedisPropertiesRefresher {

    /**
     * Spring Init Event Listener
     */
    public static class ReadyEventListener implements ApplicationListener<ApplicationReadyEvent> {

        /**
         * RedisPropertiesRefresher object
         */
        private final RedisPropertiesRefresher refresher;

        /**
         * Constructor initializers
         *
         * @param refresher RedisPropertiesRefresher object
         */
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

        /**
         * RedisPropertiesRefresher object
         */
        private final RedisPropertiesRefresher refresher;

        /**
         * Constructor initializers
         *
         * @param refresher RedisPropertiesRefresher object
         */
        public ContextClosedEventListener(RedisPropertiesRefresher refresher) {
            this.refresher = refresher;
        }

        @Override
        public void onApplicationEvent(@Nonnull ContextClosedEvent event) {
            refresher.close();
        }

    }

    /**
     * TOPIC
     */
    private static String CONFIG_TOPIC = "p6e-cloud-config";

    /**
     * TIMESTAMP
     */
    private final AtomicLong timestamp = new AtomicLong(0);

    /**
     * reactor.core.Disposable object
     */
    private reactor.core.Disposable subscription;

    /**
     * org.springframework.data.redis.core.StringRedisTemplate
     */
    private org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;

    /**
     * org.springframework.data.redis.core.ReactiveStringRedisTemplate
     */
    private org.springframework.data.redis.core.ReactiveStringRedisTemplate reactiveStringRedisTemplate;

    /**
     * Constructor initializers
     *
     * @param template  org.springframework.data.redis.core.StringRedisTemplate objcet
     * @param refresher ConfigurableApplicationContext object
     */
    public RedisPropertiesRefresher(org.springframework.data.redis.core.StringRedisTemplate template, ConfigurableApplicationContext context) {
        this.stringRedisTemplate = template;
        context.addApplicationListener(new ReadyEventListener(this));
        context.addApplicationListener(new ContextClosedEventListener(this));
    }

    /**
     * Constructor initializers
     *
     * @param template  org.springframework.data.redis.core.ReactiveStringRedisTemplate objcet
     * @param refresher ConfigurableApplicationContext object
     */
    public RedisPropertiesRefresher(org.springframework.data.redis.core.ReactiveStringRedisTemplate template, ConfigurableApplicationContext context) {
        this.reactiveStringRedisTemplate = template;
        context.addApplicationListener(new ReadyEventListener(this));
        context.addApplicationListener(new ContextClosedEventListener(this));
    }

    /**
     * Set Config Topic
     *
     * @param topic Topic object
     */
    public static void setConfigTopic(String topic) {
        CONFIG_TOPIC = topic;
    }

    /**
     * Init Data
     */
    protected void init() {
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
     * Close
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
            reactiveStringRedisTemplate = null;
        } catch (Exception e) {
            // ignore exception
        }
    }

    /**
     * Execute Message
     */
    protected void execute(Map<String, String> message) {
        if (message != null && message.get("type") != null) {
            if ("heartbeat".equalsIgnoreCase(message.get("type"))) {
                if (timestamp.get() <= 0) {
                    synchronized (this) {
                        if (timestamp.get() <= 0) {
                            try {
                                Class.forName("org.springframework.data.redis.core.StringRedisTemplate");
                                stringRedisTemplate.convertAndSend(CONFIG_TOPIC, JsonUtil.toJson(new HashMap<>() {{
                                    put("type", "init");
                                }}));
                                return;
                            } catch (Exception e) {
                                // ignore exception
                            }
                            try {
                                Class.forName("org.springframework.data.redis.core.ReactiveStringRedisTemplate");
                                reactiveStringRedisTemplate.convertAndSend(CONFIG_TOPIC, JsonUtil.toJson(new HashMap<>() {{
                                    put("type", "init");
                                }})).subscribe();
                            } catch (Exception e) {
                                // ignore exception
                            }
                        }
                    }
                }
            } else if ("config".equalsIgnoreCase(message.get("type"))
                    && message.get("format") != null && message.get("content") != null) {
                synchronized (this) {
                    // refresh timestamp
                    timestamp.set(System.currentTimeMillis());
                    config(message.get("format"), message.get("content"));
                }
            }
        }
    }

    /**
     * Config
     */
    protected abstract void config(String format, String content);

}
