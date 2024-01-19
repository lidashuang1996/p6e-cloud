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
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.io.SequenceInputStream;
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
 * 自定义日志过滤器
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
     * 执行顺序
     */
    private static final int ORDER = -3000;

    /**
     * 用户的信息头
     */
    @SuppressWarnings("ALL")
    private static final String USER_INFO_HEADER = "P6e-User-Info";

    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomLogWebFilter.class);

    /**
     * DATA BUFFER 工厂
     */
    private static final DataBufferFactory DATA_BUFFER_FACTORY = new DefaultDataBufferFactory();

    /**
     * 配置对象
     */
    private final Properties properties;

    /**
     * 构造方法初始化
     *
     * @param properties 配置对象
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
            // 创建日志模型
            final Model model = new Model();
            // 请求日志处理
            final ServerHttpRequest request = new LogServerHttpRequestDecorator(exchange, model);
            // 结果日志处理
            final ServerHttpResponse response = new LogServerHttpResponseDecorator(exchange, model, properties);
            // 继续执行
            return chain.filter(exchange.mutate().request(request).response(response).build());
        } else {
            return chain.filter(exchange);
        }
    }

    /**
     * 日志模型
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
     * 日志 Request 解码器
     */
    private static class LogServerHttpRequestDecorator extends ServerHttpRequestDecorator {
        /**
         * 模型对象
         */
        private final Model model;

        /**
         * 返回的 Request 对象
         */
        private final ServerHttpRequest request;

        /**
         * 构造方法初始化
         *
         * @param exchange ServerWebExchange 对象
         * @param model    对象
         */
        public LogServerHttpRequestDecorator(ServerWebExchange exchange, Model model) {
            super(exchange.getRequest());
            final ServerHttpRequest request = exchange.getRequest();
            this.model = model;
            this.request = exchange.getRequest();

            // 初始化写入数据
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
            return super.getBody()
                    .reduce(SequenceInputStream.nullInputStream(),
                            (s, d) -> new SequenceInputStream(s, d.asInputStream()))
                    .publishOn(Schedulers.boundedElastic())
                    .map(inputStream -> {
                        int len;
                        final byte[] b = new byte[1024];
                        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        try {
                            while ((len = inputStream.read(b)) != -1) {
                                byteArrayOutputStream.write(b, 0, len);
                            }
                        } catch (Exception e) {
                            LOGGER.error("[LOG INPUT ERROR]", e);
                        }
                        return byteArrayOutputStream.toByteArray();
                    })
                    .map(bytes -> {
                        // 写入指定的请求数据类型
                        final Map<String, String> rBodyMap = new HashMap<>(3);
                        final List<String> types = request.getHeaders().get(HttpHeaders.CONTENT_TYPE);
                        if (types == null || types.isEmpty()) {
                            rBodyMap.put("type", "unknown");
                            rBodyMap.put("size", String.valueOf(bytes.length));
                        } else {
                            final String type = types.get(0);
                            rBodyMap.put("type", type);
                            rBodyMap.put("size", String.valueOf(bytes.length));
                            // 如果请求的类型为 JSON / FORM 那么就打印全部的信息
                            if (type.startsWith(MediaType.APPLICATION_JSON_VALUE)
                                    || type.startsWith(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
                                rBodyMap.put("content", new String(bytes, StandardCharsets.UTF_8)
                                        .replaceAll("\r", "").replaceAll("\n", ""));
                            } else {
                                final int length = Math.min(1024, bytes.length);
                                final byte[] cBytes = new byte[length];
                                System.arraycopy(bytes, 0, cBytes, 0, length);
                                rBodyMap.put("content", new String(cBytes, StandardCharsets.UTF_8));
                            }
                        }
                        model.setRequestBody(JsonUtil.toJson(rBodyMap));
                        return DATA_BUFFER_FACTORY.wrap(bytes);
                    })
                    .flux();
        }

        /**
         * 本机 IP
         */
        @SuppressWarnings("ALL")
        private static final String LOCAL_IP = "127.0.0.1";

        /**
         * 未知 IP
         */
        @SuppressWarnings("ALL")
        private final static String IP_UNKNOWN = "unknown";

        /**
         * IP 请求头
         */
        @SuppressWarnings("ALL")
        private static final String IP_HEADER_X_REQUEST_IP = "x-request-ip";

        /**
         * IP 请求头
         */
        @SuppressWarnings("ALL")
        private static final String IP_HEADER_X_FORWARDED_FOR = "x-forwarded-for";

        /**
         * IP 请求头
         */
        @SuppressWarnings("ALL")
        private static final String IP_HEADER_PROXY_CLIENT_IP = "proxy-client-ip";

        /**
         * IP 请求头
         */
        @SuppressWarnings("ALL")
        private static final String IP_HEADER_WL_PROXY_CLIENT_IP = "wl-proxy-client-ip";

        /**
         * 获取用户的 IP 地址
         *
         * @param request Request 对象
         * @return IP 地址
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
                            LOGGER.error("[LOG IP ERROR]", e);
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
     * 日志 Response 解码器
     */
    private static class LogServerHttpResponseDecorator extends ServerHttpResponseDecorator {

        /**
         * 模型对象
         */
        private final Model model;

        /**
         * 配置对象
         */
        private final Properties properties;

        /**
         * 返回的 Request 对象
         */
        private final ServerHttpRequest request;

        /**
         * 返回的 Response 对象
         */
        private final ServerHttpResponse response;

        /**
         * 构造方法初始化
         *
         * @param exchange   ServerWebExchange 对象
         * @param model      对象
         * @param properties 配置对象
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
                        .map(dataBuffer -> {
                            // 读取数据
                            final byte[] bytes = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(bytes);
                            // 释放掉内存
                            DataBufferUtils.release(dataBuffer);
                            return bytes;
                        })
                        .defaultIfEmpty(new byte[0])
                        .flatMap(bytes -> {
                            // 重新写入到返回
                            // 写入指定的请求数据类型
                            final Map<String, String> rBodyMap = new HashMap<>(3);
                            final List<String> types = response.getHeaders().get(HttpHeaders.CONTENT_TYPE);
                            if (types == null || types.isEmpty()) {
                                rBodyMap.put("type", "unknown");
                                rBodyMap.put("size", String.valueOf(bytes.length));
                            } else {
                                final String type = types.get(0);
                                rBodyMap.put("type", type);
                                rBodyMap.put("size", String.valueOf(bytes.length));
                                // 如果请求的类型为 JSON / FORM 那么就打印全部的信息
                                if (type.startsWith(MediaType.APPLICATION_JSON_VALUE)) {
                                    rBodyMap.put("content", new String(bytes, StandardCharsets.UTF_8)
                                            .replaceAll("\r", "").replaceAll("\n", ""));
                                } else {
                                    final int length = Math.min(1024, bytes.length);
                                    final byte[] cBytes = new byte[length];
                                    System.arraycopy(bytes, 0, cBytes, 0, length);
                                    rBodyMap.put("content", new String(cBytes, StandardCharsets.UTF_8));
                                }
                            }
                            model.setResponseDateTime(LocalDateTime.now());
                            model.setResponseBody(JsonUtil.toJson(rBodyMap));
                            model.setResponseHeaders(JsonUtil.toJson(response.getHeaders()));
                            model.setResponseCookies(JsonUtil.toJson(response.getCookies()));
                            // 从请求头中获取最新请求头数据里面的用户信息
                            // ===== USER INFO ========================================
                            final List<String> userInfoData = request.getHeaders().get(USER_INFO_HEADER);
                            if (userInfoData != null) {
                                model.setUser(JsonUtil.toJson(userInfoData));
                            }
                            // ===== USER INFO ========================================
                            if (model.getRequestDateTime() != null && model.getResponseDateTime() != null) {
                                final long s = model.getRequestDateTime().toInstant(ZoneOffset.of("+8")).toEpochMilli();
                                final long e = model.getResponseDateTime().toInstant(ZoneOffset.of("+8")).toEpochMilli();
                                // 写入请求间隔的时间
                                model.setIntervalDateTime(e - s);
                            }
                            if (properties.getLog().isDetails()) {
                                LOGGER.info(model.toString());
                            } else {
                                LOGGER.info("[" + model.getRequestMethod() + "] "
                                        + model.getPath() + " \r\n ::: " + model.getUser());
                            }
                            return Mono.just(DATA_BUFFER_FACTORY.wrap(bytes));
                        }));
            } else {
                return super.writeWith(body);
            }
        }
    }

}
