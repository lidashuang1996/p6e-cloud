package club.p6e.cloud.websocket;

import club.p6e.cloud.websocket.dubbo.DubboCr;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author lidashuang
 * @version 1.0.0
 */
@EnableDubbo
@SpringBootApplication
public class P6eCloudWebSocketApplication {

    public static void main(String[] args) {
        SpringApplication.run(P6eCloudWebSocketApplication.class, args).getBean(DubboCr.class).send();

    }

}
