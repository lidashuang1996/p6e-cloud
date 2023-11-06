package club.p6e.cloud.file;

import club.p6e.cloud.file.aspect.*;
import club.p6e.cloud.file.cache.AuthRedisCache;
import club.p6e.cloud.file.cache.VoucherRedisCache;
import club.p6e.cloud.file.controller.AuthController;
import club.p6e.coat.file.EnableP6eFile;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author lidashuang
 * @version 1.0
 */
@Documented
@EnableP6eFile
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({
        AuthController.class,
        AuthRedisCache.class,
        VoucherRedisCache.class,
        AuthCloseUploadAspectImpl.class,
        AuthDownloadAspectImpl.class,
        AuthOpenUploadAspectImpl.class,
        AuthResourceAspectImpl.class,
        AuthSimpleUploadAspectImpl.class,
        AuthSliceUploadAspectImpl.class
})
public @interface EnableP6eCloudFile {
}
