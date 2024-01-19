package club.p6e.cloud.file.utils;

import java.util.Map;

/**
 * @author lidashuang
 * @version 1.0
 */
public class YamlUtil {

    public static Object paths(Object data, String prefix) {
        Object tmp = data;
        final String[] paths = prefix.split("\\.");
        for (final String path : paths) {
            if (tmp == null) {
                return null;
            } else {
                tmp = get(path, TransformationUtil.objectToMap(tmp));
            }
        }
        return tmp;
    }

    public static Object get(String name, Map<String, Object> data) {
        name = PropertiesUtil.formatName(name);
        for (final String key : data.keySet()) {
            if (PropertiesUtil.formatName(key).equals(name)) {
                return data.get(key);
            }
        }
        return null;
    }

}
