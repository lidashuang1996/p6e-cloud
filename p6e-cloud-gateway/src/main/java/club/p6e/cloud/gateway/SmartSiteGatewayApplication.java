package club.p6e.cloud.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 网关程序入口
 *
 * @author lidashuang
 * @version 1.0
 */
@SpringBootApplication
public class SmartSiteGatewayApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmartSiteGatewayApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SmartSiteGatewayApplication.class, args);
    }

}
