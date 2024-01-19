package club.p6e.cloud.file.utils;

import java.util.*;

/**
 * @author lidashuang
 * @version 1.0
 */
public class PropertiesUtil {

    public static Properties matchProperties(String prefix, Properties properties) {
        return matchProperties(prefix, properties, true);
    }

    public static Properties matchProperties(String prefix, Properties properties, boolean isCompleteMatch) {
        if (prefix == null || prefix.isEmpty()) {
            return properties;
        } else {
            prefix = prefix + (isCompleteMatch ? "." : "");
            final int prefixLength = prefix.length();
            final Properties result = new Properties();
            for (final String name : properties.stringPropertyNames()) {
                if (name.startsWith(prefix)) {
                    result.setProperty(name.substring(prefixLength), properties.getProperty(name));
                }
            }
            return result;
        }
    }

    public static String formatName(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        } else {
            final StringBuilder result = new StringBuilder();
            for (int i = 0; i < name.length(); i++) {
                final char ch = name.charAt(i);
                if (ch >= 'A' && ch <= 'Z') {
                    result.append("-");
                }
                result.append(String.valueOf(ch).toLowerCase());
            }
            return result.toString();
        }
    }

    public static Object getProperty(Properties properties, String name) {
        if (properties != null && !name.isEmpty()) {
            name = formatName(name);
            for (final String n : properties.stringPropertyNames()) {
                if (formatName(n).equals(name)) {
                    return properties.get(name);
                }
            }
        }
        return null;
    }

    public static List<Object> getListProperty(Properties properties, String name) {
        if (properties != null && !name.isEmpty()) {
            name = formatName(name);
            final Map<String, Object> result = new HashMap<>();
            for (final String n : properties.stringPropertyNames()) {
                final String fn = formatName(n);
                boolean status = fn.length() > name.length();
                if (status) {
                    boolean next = false;
                    final StringBuilder index = new StringBuilder();
                    final StringBuilder other = new StringBuilder();
                    for (int i = 0; i < fn.length(); i++) {
                        final String cfn = String.valueOf(fn.charAt(i));
                        if (i < name.length()) {
                            if (!cfn.equals(String.valueOf(name.charAt(i)))) {
                                status = false;
                                break;
                            }
                        } else if (i == name.length()) {
                            if (!cfn.equals("[")) {
                                status = false;
                                break;
                            }
                        } else {
                            if (next) {
                                other.append(cfn);
                            } else {
                                if (cfn.equals(".")) {
                                    status = false;
                                    break;
                                } else if (cfn.equals("]")) {
                                    next = true;
                                } else {
                                    index.append(cfn);
                                }
                            }
                        }
                    }
                    if (status) {
                        if (other.isEmpty()) {
                            result.put(index.toString(), properties.get(n));
                        } else {
                            final Object o = result.computeIfAbsent(index.toString(), k -> new HashMap<>());
                            final Map<String, Object> data = TransformationUtil.objectToMap(o);
                            data.put(other.substring(1), properties.get(n));
                        }
                    }
                }
            }
            final List<Object> list = new ArrayList<>();
            final List<String> keys = new ArrayList<>(result.keySet());
            final List<Integer> integers = keys.stream().map(Integer::parseInt).sorted().toList();
            for (final Integer item : integers) {
                list.add(result.get(String.valueOf(item)));
            }
            return list;
        }
        return null;
    }

    public static Long getLongProperty(Properties properties, String name) {
        return TransformationUtil.objectToLong(getProperty(properties, name));
    }

    public static Long getLongProperty(Properties properties, String name, Long def) {
        final Long data = getLongProperty(properties, name);
        return data == null ? def : data;
    }

    public static Integer getIntegerProperty(Properties properties, String name) {
        return TransformationUtil.objectToInteger(getProperty(properties, name));
    }

    public static Integer getIntegerProperty(Properties properties, String name, Integer def) {
        final Integer data = getIntegerProperty(properties, name);
        return data == null ? def : data;
    }

    public static String getStringProperty(Properties properties, String name) {
        return TransformationUtil.objectToString(getProperty(properties, name));
    }

    public static String getStringProperty(Properties properties, String name, String def) {
        final String data = getStringProperty(properties, name);
        return data == null ? def : data;
    }

    public static Boolean getBooleanProperty(Properties properties, String name) {
        return TransformationUtil.objectToBoolean(getProperty(properties, name));
    }

    public static Boolean getBooleanProperty(Properties properties, String name, Boolean def) {
        final Boolean data = getBooleanProperty(properties, name);
        return data == null ? def : data;
    }

    public static List<Long> getListLongProperty(Properties properties, String name) {
        return getListProperty(properties, name).stream().map(TransformationUtil::objectToLong).toList();
    }

    public static List<Long> getListLongProperty(Properties properties, String name, List<Long> def) {
        final List<Long> data = getListLongProperty(properties, name);
        return (data == null || data.isEmpty()) ? def : data;
    }

    public static List<Integer> getListIntegerProperty(Properties properties, String name) {
        return getListProperty(properties, name).stream().map(TransformationUtil::objectToInteger).toList();
    }

    public static List<Integer> getListIntegerProperty(Properties properties, String name, List<Integer> def) {
        final List<Integer> data = getListIntegerProperty(properties, name);
        return (data == null || data.isEmpty()) ? def : data;
    }

    public static List<String> getListStringProperty(Properties properties, String name) {
        return getListProperty(properties, name).stream().map(TransformationUtil::objectToString).toList();
    }

    public static List<String> getListStringProperty(Properties properties, String name, List<String> def) {
        final List<String> data = getListStringProperty(properties, name);
        return (data == null || data.isEmpty()) ? def : data;
    }

    public static List<Boolean> getListBooleanProperty(Properties properties, String name) {
        return getListProperty(properties, name).stream().map(TransformationUtil::objectToBoolean).toList();
    }

    public static List<Boolean> getListBooleanProperty(Properties properties, String name, List<Boolean> def) {
        final List<Boolean> data = getListBooleanProperty(properties, name);
        return (data == null || data.isEmpty()) ? def : data;
    }

    public static List<Object> getListObjectProperty(Properties properties, String name) {
        return getListProperty(properties, name);
    }

    public static List<Object> getListObjectProperty(Properties properties, String name, List<Object> def) {
        final List<Object> data = getListObjectProperty(properties, name);
        return (data == null || data.isEmpty()) ? def : data;
    }

    public static List<Properties> getListPropertiesProperty(Properties properties, String name) {
        return getListProperty(properties, name)
                .stream()
                .map(i -> {
                    final Properties ps = new Properties();
                    if (i instanceof final Map<?, ?> map) {
                        ps.putAll(map);
                    }
                    return ps;
                }).toList();
    }

    public static Map<String, Object> getMapProperty(Properties properties, String name) {
        final Map<String, Object> result = new HashMap<>();
        final Properties matchProperties = matchProperties(name, properties, true);
        for (final String n : matchProperties.stringPropertyNames()) {
            result.put(n, matchProperties.get(n));
        }
        return result;
    }

    public static Map<String, Object> getMapProperty(Properties properties, String name, Map<String, Object> def) {
        final Map<String, Object> data = getMapProperty(properties, name);
        return data.isEmpty() ? def : data;
    }

}
