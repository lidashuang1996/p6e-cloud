package club.p6e.cloud.websocket.controller.support.filter;

import club.p6e.coat.common.Properties;
import club.p6e.coat.common.controller.filter.VoucherWebFilter;
import jakarta.servlet.annotation.WebFilter;
import org.springframework.stereotype.Component;

/**
 * 凭证的过滤器
 *
 * @author lidashuang
 * @version 1.0
 */
@Component
@WebFilter(filterName = "VoucherFilter", urlPatterns = {"*"})
public class CustomVoucherFilter extends VoucherWebFilter {

    public CustomVoucherFilter(Properties properties) {
        super(properties);
    }

}
