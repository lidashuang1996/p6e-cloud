package club.p6e.cloud.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;

/**
 * 网关程序入口
 *
 * @author lidashuang
 * @version 1.0
 */
@EnableScheduling
@SpringBootApplication
public class SmartSiteGatewayApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmartSiteGatewayApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SmartSiteGatewayApplication.class, args);
//        LOGGER.info("=======================================================");
//        final Properties properties = SpringUtil.getBean(Properties.class);
//        LOGGER.info("=======================================================");
//        LOGGER.info("=====   " + properties.getClass() + "   =====");
//        LOGGER.info("=======================================================");
//        LOGGER.info("Version >> " + properties.getVersion());
//        LOGGER.info("Header Prefix >> " + properties.getHeaderPrefix());
//        LOGGER.info("Referer >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ");
//        LOGGER.info("Referer Enabled >> " + properties.getReferer().isEnabled());
//        LOGGER.info("Referer White List >> " + Arrays.toString(properties.getReferer().getWhiteList()));
//        LOGGER.info("Cross Domain >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ");
//        LOGGER.info("Cross Domain Enabled >> " + properties.getCrossDomain().isEnabled());
//        LOGGER.info("=======================================================");
//        LOGGER.info("=======================================================");
//        LOGGER.info("================ PASS WORD [ 123456  ]=================");
//        LOGGER.info(UserAuthRepository.encryptionPassword("123456"));
//        LOGGER.info("=======================================================");
    }

}
