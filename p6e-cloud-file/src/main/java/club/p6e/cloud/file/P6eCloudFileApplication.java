package club.p6e.cloud.file;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author lidashuang
 * @version 1.0
 */
@EnableP6eCloudFile
@SpringBootApplication
public class P6eCloudFileApplication {
    public static void main(String[] args) {
        SpringApplication.run(P6eCloudFileApplication.class, args);
    }

}

