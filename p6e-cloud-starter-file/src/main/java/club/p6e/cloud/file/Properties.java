package club.p6e.cloud.file;

import club.p6e.cloud.file.utils.PropertiesUtil;
import club.p6e.cloud.file.utils.TransformationUtil;
import club.p6e.cloud.file.utils.YamlUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 配置文件
 *
 * @author lidashuang
 * @version 1.0
 */
@Data
@Component
@Accessors(chain = true)
@ConfigurationProperties(prefix = "p6e.cloud.file")
public class Properties implements Serializable {

    private static void initBase(
            Properties properties,
            String version,
            Boolean refererEnable,
            List<Object> refererWhiteList,
            Boolean crossDomainEnable,
            List<Object> crossDomainWhiteList,
            String sliceUploadPath,
            Long sliceUploadMaxSize
    ) {
        if (version != null) {
            properties.setVersion(version);
        }
        if (refererEnable != null) {
            properties.getReferer().setEnable(refererEnable);
        }
        if (refererWhiteList != null) {
            final List<String> tmpList = new ArrayList<>();
            for (Object item : refererWhiteList) {
                tmpList.add(TransformationUtil.objectToString(item));
            }
            properties.getReferer().setWhiteList(tmpList);
        }
        if (crossDomainEnable != null) {
            properties.getCrossDomain().setEnable(crossDomainEnable);
        }
        if (crossDomainWhiteList != null) {
            final List<String> tmpList = new ArrayList<>();
            for (Object item : crossDomainWhiteList) {
                tmpList.add(TransformationUtil.objectToString(item));
            }
            properties.getCrossDomain().setWhiteList(tmpList);
        }
        if (sliceUploadPath != null) {
            properties.getSliceUpload().setPath(sliceUploadPath);
        }
        if (sliceUploadMaxSize != null) {
            properties.getSliceUpload().setMaxSize(sliceUploadMaxSize);
        }
    }

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

    public static Properties initYaml(Object data) {
        final Properties result = new Properties();
        final Object config = YamlUtil.paths(data, "p6e.cloud.file");
        final Map<String, Object> cmap = TransformationUtil.objectToMap(config);
        final String version = TransformationUtil.objectToString(YamlUtil.paths(cmap, "version"));
        final Boolean refererEnable = TransformationUtil.objectToBoolean(YamlUtil.paths(cmap, "referer.enable"));
        final List<Object> refererWhiteList = TransformationUtil.objectToList(YamlUtil.paths(cmap, "referer.whiteList"));
        final Boolean crossDomainEnable = TransformationUtil.objectToBoolean(YamlUtil.paths(cmap, "crossDomain.enable"));
        final List<Object> crossDomainWhiteList = TransformationUtil.objectToList(YamlUtil.paths(cmap, "crossDomain.whiteList"));
        final String sliceUploadPath = TransformationUtil.objectToString(YamlUtil.paths(cmap, "sliceUpload.path"));
        final Long sliceUploadMaxSize = TransformationUtil.objectToLong(YamlUtil.paths(cmap, "sliceUpload.maxSize"));
        initBase(result, version, refererEnable, refererWhiteList,
                crossDomainEnable, crossDomainWhiteList, sliceUploadPath, sliceUploadMaxSize);
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

    public static Properties initProperties(java.util.Properties properties) {
        final Properties result = new Properties();
        properties = PropertiesUtil.matchProperties("p6e.cloud.file", properties);
        final String version = PropertiesUtil.getStringProperty(properties, "version");
        final java.util.Properties refererProperties = PropertiesUtil.matchProperties("referer", properties);
        final Boolean refererEnable = PropertiesUtil.getBooleanProperty(refererProperties, "enable");
        final List<Object> refererWhiteList = PropertiesUtil.getListObjectProperty(refererProperties, "whiteList");
        final java.util.Properties crossDomainProperties = PropertiesUtil.matchProperties("crossDomain", properties);
        final Boolean crossDomainEnable = PropertiesUtil.getBooleanProperty(crossDomainProperties, "enable");
        final List<Object> crossDomainWhiteList = PropertiesUtil.getListObjectProperty(crossDomainProperties, "whiteList");
        final java.util.Properties sliceUploadProperties = PropertiesUtil.matchProperties("sliceUpload", properties);
        final String sliceUploadPath = PropertiesUtil.getStringProperty(sliceUploadProperties, "path");
        final Long sliceUploadMaxSize = PropertiesUtil.getLongProperty(sliceUploadProperties, "maxSize");
        initBase(result, version, refererEnable, refererWhiteList,
                crossDomainEnable, crossDomainWhiteList, sliceUploadPath, sliceUploadMaxSize);
        final Map<String, Object> uploads = PropertiesUtil.getMapProperty(properties, "uploads");
        result.setUploads(initUploads(uploads));
        final Map<String, Object> resources = PropertiesUtil.getMapProperty(properties, "resources");
        result.setResources(initResources(resources));
        final Map<String, Object> downloads = PropertiesUtil.getMapProperty(properties, "downloads");
        result.setDownloads(initDownloads(downloads));
        return result;
    }

    private String version = "unknown";
    private club.p6e.coat.file.Properties.Referer referer = new club.p6e.coat.file.Properties.Referer();
    private club.p6e.coat.file.Properties.CrossDomain crossDomain = new club.p6e.coat.file.Properties.CrossDomain();
    private club.p6e.coat.file.Properties.SliceUpload sliceUpload = new club.p6e.coat.file.Properties.SliceUpload();
    private Map<String, club.p6e.coat.file.Properties.Upload> uploads = new HashMap<>();
    private Map<String, club.p6e.coat.file.Properties.Resource> resources = new HashMap<>();
    private Map<String, club.p6e.coat.file.Properties.Download> downloads = new HashMap<>();
}