package club.p6e.cloud.websocket;

import club.p6e.coat.websocket.EnableP6eWebSocket;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author lidashuang
 * @version 1.0.0
 */
@EnableDubbo
@EnableP6eWebSocket
@SpringBootApplication
public class P6eCloudWebSocketApplication {

    public static void main(String[] args) {
        SpringApplication.run(P6eCloudWebSocketApplication.class, args);
    }

}
