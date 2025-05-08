package club.p6e.cloud.gateway.filter;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.Serializable;

/**
 * Voucher Gateway Filter Factory
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
public class VoucherGatewayFilterFactory extends AbstractGatewayFilterFactory<VoucherGatewayFilterFactory.Config> {

    /**
     * P6e Voucher Header Name
     */
    private static final String VOUCHER_HEADER = "P6e-Voucher";

    /**
     * Constructor initializers
     */
    public VoucherGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return new CustomGatewayFilter(config);
    }

    /**
     * Custom Gateway Filter
     */
    public static class CustomGatewayFilter implements GatewayFilter {

        /**
         * Config object
         */
        private final Config config;

        /**
         * Constructor initializers
         *
         * @param config Config object
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

    }

    /**
     * Config
     */
    @Data
    @Accessors(chain = true)
    public static class Config implements Serializable {

        /**
         * Voucher
         */
        private String voucher;

    }

}
