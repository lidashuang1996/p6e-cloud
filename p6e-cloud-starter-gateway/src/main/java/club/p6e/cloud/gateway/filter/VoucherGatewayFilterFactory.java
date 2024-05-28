package club.p6e.cloud.gateway.filter;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.Serializable;

/**
 * 凭证过滤器网关
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
public class VoucherGatewayFilterFactory
        extends AbstractGatewayFilterFactory<VoucherGatewayFilterFactory.Config> {

    /**
     * 顺序
     */
    private static final int ORDER = -1300;

    /**
     * 凭证的头部
     */
    private static final String VOUCHER_HEADER = "P6e-Voucher";

    /**
     * 构造方法
     */
    public VoucherGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return new CustomGatewayFilter(config);
    }

    /**
     * 网关过滤器
     */
    @SuppressWarnings("ALL")
    public static class CustomGatewayFilter implements GatewayFilter, Ordered {

        /**
         * 配置文件对象
         */
        private final Config config;

        /**
         * 构造方法初始化
         *
         * @param config 配置文件对象
         */
        public CustomGatewayFilter(Config config) {
            this.config = config;
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            final ServerHttpRequest.Builder builder = exchange.getRequest().mutate();
            builder.header(VOUCHER_HEADER, (config.getVoucher() == null ? "" : config.getVoucher()));
            return chain.filter(exchange.mutate().request(builder.build()).build());
        }

        @Override
        public int getOrder() {
            return ORDER;
        }
    }

    @Data
    @Accessors(chain = true)
    public static class Config implements Serializable {
        private String voucher;
    }

}
