package club.p6e.cloud.file;

import club.p6e.coat.common.utils.PropertiesUtil;
import club.p6e.coat.common.utils.TransformationUtil;
import club.p6e.coat.common.utils.YamlUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Properties
 *
 * @author lidashuang
 * @version 1.0
 */
@Data
@Primary
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Component("club.p6e.cloud.file.Properties")
@ConfigurationProperties(prefix = "p6e.cloud.file")
public class Properties extends club.p6e.coat.file.Properties implements Serializable {

    /**
     * Secret
     */
    private String secret;

    /**
     * INIT BASE
     */
    private static void initBase(
            Properties properties,
            String sliceUploadPath,
            Long sliceUploadMaxSize
    ) {
        if (sliceUploadPath != null) {
            properties.getSliceUpload().setPath(sliceUploadPath);
        }
        if (sliceUploadMaxSize != null) {
            properties.getSliceUpload().setMaxSize(sliceUploadMaxSize);
        }
    }

    /**
     * INIT UPLOADS
     */
    private static Map<String, club.p6e.coat.file.Properties.Upload> initUploads(Map<String, Object> data) {
        final Map<String, club.p6e.coat.file.Properties.Upload> result = new HashMap<>();
        for (final String key : data.keySet()) {
            final Map<String, Object> map = TransformationUtil.objectToMap(data.get(key));
            if (map != null) {
                final club.p6e.coat.file.Properties.Upload upload = new club.p6e.coat.file.Properties.Upload();
                final String type = TransformationUtil.objectToString(map.get("type"));
                final String path = TransformationUtil.objectToString(map.get("path"));
                final Map<String, Object> extend = TransformationUtil.objectToMap(map.get("extend"));
                if (type != null) {
                    upload.setType(type);
                }
                if (path != null) {
                    upload.setPath(path);
                }
                if (extend != null) {
                    final Map<String, String> tmp = new HashMap<>();
                    extend.forEach((k, v) -> tmp.put(k, TransformationUtil.objectToString(v)));
                    upload.setExtend(tmp);
                }
                result.put(key, upload);
            }
        }
        return result;
    }

    /**
     * INIT DOWNLOADS
     */
    private static Map<String, club.p6e.coat.file.Properties.Download> initDownloads(Map<String, Object> downloads) {
        final Map<String, club.p6e.coat.file.Properties.Download> result = new HashMap<>();
        for (final String key : downloads.keySet()) {
            final Map<String, Object> value = TransformationUtil.objectToMap(downloads.get(key));
            if (value != null) {
                final club.p6e.coat.file.Properties.Download download = new club.p6e.coat.file.Properties.Download();
                final Map<String, Object> extend = TransformationUtil.objectToMap(value.get("extend"));
                if (extend != null) {
                    final Map<String, String> tmp = new HashMap<>();
                    extend.forEach((k, v) -> tmp.put(k, TransformationUtil.objectToString(v)));
                    download.setExtend(tmp);
                }
                final String type = TransformationUtil.objectToString(value.get("type"));
                if (type != null) {
                    download.setType(type);
                }
                final String path = TransformationUtil.objectToString(value.get("path"));
                if (path != null) {
                    download.setPath(path);
                }
                result.put(key, download);
            }
        }
        return result;
    }

    /**
     * INIT RESOURCES
     */
    private static Map<String, club.p6e.coat.file.Properties.Resource> initResources(Map<String, Object> resources) {
        final Map<String, club.p6e.coat.file.Properties.Resource> result = new HashMap<>();
        for (final String key : resources.keySet()) {
            final Map<String, Object> value = TransformationUtil.objectToMap(resources.get(key));
            if (value != null) {
                final club.p6e.coat.file.Properties.Resource resource = new club.p6e.coat.file.Properties.Resource();
                final String type = TransformationUtil.objectToString(value.get("type"));
                final Map<String, Object> extend = TransformationUtil.objectToMap(value.get("extend"));
                final String path = TransformationUtil.objectToString(value.get("path"));
                if (type != null) {
                    resource.setType(type);
                }
                if (extend != null) {
                    final Map<String, String> tmp = new HashMap<>();
                    extend.forEach((k, v) -> tmp.put(k, TransformationUtil.objectToString(v)));
                    resource.setExtend(tmp);
                }
                if (path != null) {
                    resource.setPath(path);
                }
                final Map<String, Object> suffixes = TransformationUtil.objectToMap(value.get("suffixes"));
                if (suffixes != null) {
                    final Map<String, MediaType> tmp = new HashMap<>();
                    suffixes.forEach((k, v) -> {
                        try {
                            tmp.put(k, MediaType.valueOf(TransformationUtil.objectToString(v)));
                        } catch (Exception e) {
                            // ignore
                        }
                    });
                    resource.setSuffixes(tmp);
                }
                result.put(key, resource);
            }
        }
        return result;
    }

    /**
     * INIT YAML
     */
    @SuppressWarnings("ALL")
    public static Properties initYaml(Object data) {
        final Properties result = new Properties();
        final Object config = YamlUtil.paths(data, "p6e.cloud.file");
        final Map<String, Object> cmap = TransformationUtil.objectToMap(config);
        final String sliceUploadPath = TransformationUtil.objectToString(YamlUtil.paths(cmap, "slice-upload.path"));
        final Long sliceUploadMaxSize = TransformationUtil.objectToLong(YamlUtil.paths(cmap, "slice-upload.maxSize"));
        initBase(result, sliceUploadPath, sliceUploadMaxSize);
        final String secret = TransformationUtil.objectToString(YamlUtil.paths(cmap, "secret"));
        if (secret != null) {
            result.setSecret(secret);
        }
        final Map<String, Object> uploads = TransformationUtil.objectToMap(YamlUtil.paths(cmap, "uploads"));
        if (uploads != null) {
            result.setUploads(initUploads(uploads));
        }
        final Map<String, Object> resources = TransformationUtil.objectToMap(YamlUtil.paths(cmap, "resources"));
        if (resources != null) {
            result.setResources(initResources(resources));
        }
        final Map<String, Object> downloads = TransformationUtil.objectToMap(YamlUtil.paths(cmap, "downloads"));
        if (downloads != null) {
            result.setDownloads(initDownloads(downloads));
        }
        return result;
    }

    /**
     * INIT PROPERTIES
     */
    @SuppressWarnings("ALL")
    public static Properties initProperties(java.util.Properties properties) {
        final Properties result = new Properties();
        properties = PropertiesUtil.matchProperties("p6e.cloud.file", properties);
        final java.util.Properties sliceUploadProperties = PropertiesUtil.matchProperties("slice-upload", properties);
        final String sliceUploadPath = PropertiesUtil.getStringProperty(sliceUploadProperties, "path");
        final Long sliceUploadMaxSize = PropertiesUtil.getLongProperty(sliceUploadProperties, "max-size");
        initBase(result, sliceUploadPath, sliceUploadMaxSize);
        final String secret = PropertiesUtil.getStringProperty(properties, "secret");
        result.setSecret(secret);
        final Map<String, Object> uploads = PropertiesUtil.getMapProperty(properties, "uploads");
        result.setUploads(initUploads(uploads));
        final Map<String, Object> resources = PropertiesUtil.getMapProperty(properties, "resources");
        result.setResources(initResources(resources));
        final Map<String, Object> downloads = PropertiesUtil.getMapProperty(properties, "downloads");
        result.setDownloads(initDownloads(downloads));
        return result;
    }

}