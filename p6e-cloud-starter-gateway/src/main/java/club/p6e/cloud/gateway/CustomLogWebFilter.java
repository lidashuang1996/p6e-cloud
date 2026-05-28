package club.p6e.cloud.gateway;

import club.p6e.coat.common.utils.JsonUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom Log Web Filter
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@ConditionalOnMissingBean(
        value = CustomLogWebFilter.class,
        ignored = CustomLogWebFilter.class
)
public class CustomLogWebFilter implements WebFilter, Ordered {

    /**
     * Order
     */
    private static final int ORDER = Integer.MAX_VALUE - 1000;

    /**
     * P6e User Info Header Name
     */
    @SuppressWarnings("ALL")
    private static final String USER_INFO_HEADER = "P6e-User-Info";

    /**
     * Inject log objects
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomLogWebFilter.class);

    /**
     * DATA_BUFFER_FACTORY
     */
    private static final DataBufferFactory DATA_BUFFER_FACTORY = new DefaultDataBufferFactory();

    /**
     * Properties object
     */
    private final Properties properties;

    /**
     * Constructor initializers
     *
     * @param properties Properties object
     */
    public CustomLogWebFilter(Properties properties) {
        this.properties = properties;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    @NonNull
    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        if (properties.getLog().isEnable()) {
            // create log model
            final Model model = new Model();
            // request log processing
            final ServerHttpRequest request = new LogServerHttpRequestDecorator(exchange, model, properties);
            // result log processing
            final ServerHttpResponse response = new LogServerHttpResponseDecorator(exchange, model, properties);
            // execute
            return chain.filter(exchange.mutate().request(request).response(response).build());
        } else {
            return chain.filter(exchange);
        }
    }

    /**
     * Log Model
     */
    @Data
    @Accessors(chain = true)
    private static class Model implements Serializable {
        private volatile String id;
        private volatile String path;
        private volatile LocalDateTime requestDateTime;
        private volatile String requestMethod;
        private volatile String requestCookies;
        private volatile String requestHeaders;
        private volatile String requestBody;
        private volatile String requestQueryParams;
        private volatile String responseBody;
        private volatile String responseHeaders;
        private volatile String responseCookies;
        private volatile LocalDateTime responseDateTime;
        private volatile long intervalDateTime;
        private volatile String ip;
        private volatile String user;

        @Override
        public String toString() {
            return JsonUtil.toJson(this);
        }

    }

    /**
     * Log Server Http Request Decorator
     */
    private static class LogServerHttpRequestDecorator extends ServerHttpRequestDecorator {

        /**
         * Log mode object
         */
        private final Model model;

        /**
         * Properties object
         */
        private final Properties properties;

        /**
         * ServerHttpRequest object
         */
        private final ServerHttpRequest request;

        /**
         * Constructor initializers
         *
         * @param model      Log mode object
         * @param exchange   ServerHttpRequest object
         * @param properties Properties object
         */
        public LogServerHttpRequestDecorator(ServerWebExchange exchange, Model model, Properties properties) {
            super(exchange.getRequest());
            final ServerHttpRequest request = exchange.getRequest();
            this.model = model;
            this.properties = properties;
            this.request = exchange.getRequest();

            // initialize writing data
            model.setIp(ip(request));
            model.setId(request.getId());
            model.setPath(request.getPath().value());
            model.setRequestDateTime(LocalDateTime.now());
            model.setRequestMethod(request.getMethod().name());
            model.setRequestCookies(JsonUtil.toJson(request.getCookies()));
            model.setRequestHeaders(JsonUtil.toJson(request.getHeaders()));
            model.setRequestQueryParams(JsonUtil.toJson(request.getQueryParams()));
        }

        @NonNull
        @Override
        public Flux<DataBuffer> getBody() {
            if (properties.getLog().isEnable()) {
                return DataBufferUtils
                        .join(super.getBody())
                        .map(buffer -> {
                            final byte[] bytes = new byte[buffer.readableByteCount()];
                            buffer.read(bytes);
                            DataBufferUtils.release(buffer);
                            return bytes;
                        })
                        .defaultIfEmpty(new byte[0])
                        .map(bytes -> {
                            final Map<String, String> rBodyMap = new HashMap<>(3);
                            final List<String> types = request.getHeaders().get(HttpHeaders.CONTENT_TYPE);
                            if (types == null || types.isEmpty()) {
                                rBodyMap.put("type", "unknown");
                                rBodyMap.put("size", String.valueOf(bytes.length));
                            } else {
                                final String type = types.get(0);
                                rBodyMap.put("type", type);
                                rBodyMap.put("size", String.valueOf(bytes.length));
                                final byte[] content = new byte[Math.min(bytes.length, 10240)];
                                System.arraycopy(bytes, 0, content, 0, content.length);
                                // If the requested type is JSON/FORM, then print all the information
                                if (type.startsWith(MediaType.APPLICATION_JSON_VALUE)
                                        || type.startsWith(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
                                    rBodyMap.put("content", new String(content, StandardCharsets.UTF_8)
                                            .replaceAll("\r", "").replaceAll("\n", ""));
                                } else {
                                    rBodyMap.put("content", new String(content, StandardCharsets.UTF_8));
                                }
                            }
                            model.setRequestBody(JsonUtil.toJson(rBodyMap));
                            return DATA_BUFFER_FACTORY.wrap(bytes);
                        })
                        .flux();
            } else {
                return super.getBody();
            }
        }

        /**
         * IP request header
         */
        @SuppressWarnings("ALL")
        private static final String LOCAL_IP = "127.0.0.1";

        /**
         * IP request header
         */
        @SuppressWarnings("ALL")
        private final static String IP_UNKNOWN = "unknown";

        /**
         * IP request header
         */
        @SuppressWarnings("ALL")
        private static final String IP_HEADER_X_REQUEST_IP = "x-request-ip";

        /**
         * IP request header
         */
        @SuppressWarnings("ALL")
        private static final String IP_HEADER_X_FORWARDED_FOR = "x-forwarded-for";

        /**
         * IP request header
         */
        @SuppressWarnings("ALL")
        private static final String IP_HEADER_PROXY_CLIENT_IP = "proxy-client-ip";

        /**
         * IP request header
         */
        @SuppressWarnings("ALL")
        private static final String IP_HEADER_WL_PROXY_CLIENT_IP = "wl-proxy-client-ip";

        /**
         * Obtain user IP address
         *
         * @param request ServerHttpRequest object
         * @return IP object
         */
        public String ip(ServerHttpRequest request) {
            final HttpHeaders httpHeaders = request.getHeaders();
            List<String> list = httpHeaders.get(IP_HEADER_X_FORWARDED_FOR);
            if (list == null || list.isEmpty() || IP_UNKNOWN.equalsIgnoreCase(list.get(0))) {
                list = httpHeaders.get(IP_HEADER_PROXY_CLIENT_IP);
            }
            if (list == null || list.isEmpty() || IP_UNKNOWN.equalsIgnoreCase(list.get(0))) {
                list = httpHeaders.get(IP_HEADER_WL_PROXY_CLIENT_IP);
            }
            if (list == null || list.isEmpty() || IP_UNKNOWN.equalsIgnoreCase(list.get(0))) {
                list = httpHeaders.get(IP_HEADER_X_REQUEST_IP);
            }
            if (list == null || list.isEmpty() || IP_UNKNOWN.equalsIgnoreCase(list.get(0))) {
                final InetSocketAddress inetSocketAddress = request.getRemoteAddress();
                if (inetSocketAddress != null
                        && inetSocketAddress.getAddress() != null
                        && inetSocketAddress.getAddress().getHostAddress() != null) {
                    final String inetSocketAddressHost = inetSocketAddress.getAddress().getHostAddress();
                    if (LOCAL_IP.equals(inetSocketAddressHost)) {
                        try {
                            list = List.of(InetAddress.getLocalHost().getHostAddress());
                        } catch (Exception e) {
                            LOGGER.error("[ LOG IP ERROR] >>> {}", e.getMessage());
                        }
                    } else {
                        list = List.of(inetSocketAddressHost);
                    }
                }
            }
            if (list != null && !list.isEmpty()) {
                return list.get(0);
            }
            return IP_UNKNOWN;
        }
    }

    /**
     * Log Server Http Response Decorator
     */
    private static class LogServerHttpResponseDecorator extends ServerHttpResponseDecorator {

        /**
         * Mode object
         */
        private final Model model;

        /**
         * Properties object
         */
        private final Properties properties;

        /**
         * ServerHttpRequest object
         */
        private final ServerHttpRequest request;

        /**
         * ServerHttpResponse object
         */
        private final ServerHttpResponse response;

        /**
         * Constructor initializers
         *
         * @param exchange   ServerWebExchange object
         * @param model      Mode object
         * @param properties Properties object
         */
        public LogServerHttpResponseDecorator(ServerWebExchange exchange, Model model, Properties properties) {
            super(exchange.getResponse());
            final ServerHttpRequest request = exchange.getRequest();
            final ServerHttpResponse response = exchange.getResponse();
            this.model = model;
            this.request = request;
            this.response = response;
            this.properties = properties;
        }

        @Override
        public @NonNull
        Mono<Void> writeWith(@NonNull Publisher<? extends DataBuffer> body) {
            if (properties.getLog().isEnable()) {
                return super.writeWith(DataBufferUtils.join(body)
                        .map(buffer -> {
                            final byte[] bytes = new byte[buffer.readableByteCount()];
                            buffer.read(bytes);
                            DataBufferUtils.release(buffer);
                            return bytes;
                        })
                        .defaultIfEmpty(new byte[0])
                        .flatMap(bytes -> {
                            final Map<String, String> rBodyMap = new HashMap<>(3);
                            final List<String> types = response.getHeaders().get(HttpHeaders.CONTENT_TYPE);
                            if (types == null || types.isEmpty()) {
                                rBodyMap.put("type", "unknown");
                                rBodyMap.put("size", String.valueOf(bytes.length));
                            } else {
                                final byte[] content = new byte[Math.min(bytes.length, 10240)];
                                System.arraycopy(bytes, 0, content, 0, content.length);
                                final String type = types.get(0);
                                rBodyMap.put("type", type);
                                rBodyMap.put("size", String.valueOf(bytes.length));
                                // if the requested type is JSON/FORM, then print all the information
                                if (type.startsWith(MediaType.APPLICATION_JSON_VALUE)) {
                                    rBodyMap.put("content", new String(content, StandardCharsets.UTF_8)
                                            .replaceAll("\r", "").replaceAll("\n", ""));
                                } else {
                                    rBodyMap.put("content", new String(content, StandardCharsets.UTF_8));
                                }
                            }
                            model.setResponseDateTime(LocalDateTime.now());
                            model.setResponseBody(JsonUtil.toJson(rBodyMap));
                            model.setResponseHeaders(JsonUtil.toJson(response.getHeaders()));
                            model.setResponseCookies(JsonUtil.toJson(response.getCookies()));
                            // retrieve the latest user information from the request header
                            // ===== USER INFO ========================================
                            final List<String> userInfoData = request.getHeaders().get(USER_INFO_HEADER);
                            if (userInfoData != null) {
                                model.setUser(JsonUtil.toJson(userInfoData));
                            }
                            // ===== USER INFO ========================================
                            if (model.getRequestDateTime() != null && model.getResponseDateTime() != null) {
                                final long s = model.getRequestDateTime().toInstant(ZoneOffset.of("+8")).toEpochMilli();
                                final long e = model.getResponseDateTime().toInstant(ZoneOffset.of("+8")).toEpochMilli();
                                // time interval for writing requests
                                model.setIntervalDateTime(e - s);
                            }
                            if (properties.getLog().isDetails()) {
                                LOGGER.info(model.toString());
                            } else {
                                LOGGER.info("[ {} ] {} \r\n USER : {}", model.getRequestMethod(), model.getPath(), model.getUser());
                            }
                            return Mono.just(DATA_BUFFER_FACTORY.wrap(bytes));
                        }));
            } else {
                return super.writeWith(body);
            }
        }
    }

}
