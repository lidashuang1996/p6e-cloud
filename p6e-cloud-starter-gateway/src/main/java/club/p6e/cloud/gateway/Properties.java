package club.p6e.cloud.gateway;

import club.p6e.coat.common.utils.GeneratorUtil;
import club.p6e.coat.common.utils.PropertiesUtil;
import club.p6e.coat.common.utils.TransformationUtil;
import club.p6e.coat.common.utils.YamlUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.net.URI;
import java.util.*;

/**
 * Properties
 *
 * @author lidashuang
 * @version 1.0
 */
@Data
@Accessors(chain = true)
@Component("club.p6e.cloud.gateway.Properties")
@ConfigurationProperties(prefix = "p6e.cloud.gateway")
public class Properties implements Serializable {

    @SuppressWarnings("ALL")
    public static Properties initYaml(Object data) {
        final Properties result = new Properties();
        final Object config = YamlUtil.paths(data, "p6e.cloud.gateway");
        final Map<String, Object> cmap = TransformationUtil.objectToMap(config);
        final Boolean logEnable = TransformationUtil.objectToBoolean(YamlUtil.paths(cmap, "log.enable"));
        final Boolean logDetails = TransformationUtil.objectToBoolean(YamlUtil.paths(cmap, "log.details"));
        final List<Object> requestHeaderClear = TransformationUtil.objectToList(YamlUtil.paths(cmap, "requestHeaderClear"));
        final List<Object> responseHeaderOnly = TransformationUtil.objectToList(YamlUtil.paths(cmap, "responseHeaderOnly"));
        initBase(result, logEnable, logDetails, requestHeaderClear, responseHeaderOnly);
        return initYamlRoutes(TransformationUtil.objectToList(YamlUtil.paths(cmap, "routes")), result);
    }

    private static Properties initYamlRoutes(List<Object> routes, Properties properties) {
        for (final Object route : routes) {
            try {
                final RouteDefinition routeDefinition = new RouteDefinition();
                final String id = TransformationUtil.objectToString(YamlUtil.paths(route, "id"));
                routeDefinition.setId(id == null ? GeneratorUtil.uuid() : id);
                final Integer order = TransformationUtil.objectToInteger(YamlUtil.paths(route, "order"));
                routeDefinition.setOrder(order == null ? 0 : order);
                routeDefinition.setUri(new URI(TransformationUtil.objectToString(YamlUtil.paths(route, "uri"))));
                final Map<String, Object> metadata = TransformationUtil.objectToMap(YamlUtil.paths(route, "metadata"));
                if (metadata != null) {
                    routeDefinition.getMetadata().putAll(metadata);
                }
                final List<Object> filters = TransformationUtil.objectToList(YamlUtil.paths(route, "filters"));
                final List<Object> predicates = TransformationUtil.objectToList(YamlUtil.paths(route, "predicates"));
                if (filters != null) {
                    for (final Object filter : filters) {
                        final FilterDefinition filterDefinition = new FilterDefinition();
                        final String name = TransformationUtil.objectToString(YamlUtil.paths(filter, "name"));
                        final Map<String, Object> omap = TransformationUtil.objectToMap(YamlUtil.paths(filter, "args"));
                        for (final String key : omap.keySet()) {
                            if (omap.get(key) instanceof List) {
                                final List<Object> list = TransformationUtil.objectToList(omap.get(key));
                                for (int i = 0; i < list.size(); i++) {
                                    filterDefinition.getArgs().put(key + "." + i, String.valueOf(list.get(i)));
                                }
                            }
                        }
                        filterDefinition.setName(name);
                        routeDefinition.getFilters().add(filterDefinition);
                    }
                }
                if (predicates != null) {
                    for (final Object predicate : predicates) {
                        final PredicateDefinition predicateDefinition = new PredicateDefinition();
                        final String name = TransformationUtil.objectToString(YamlUtil.paths(predicate, "name"));
                        final Map<String, Object> omap = TransformationUtil.objectToMap(YamlUtil.paths(predicate, "args"));
                        for (final String key : omap.keySet()) {
                            final Object value = omap.get(key);
                            if (value instanceof List) {
                                final List<Object> list = TransformationUtil.objectToList(value);
                                for (int i = 0; i < list.size(); i++) {
                                    predicateDefinition.getArgs().put(key + "." + i, String.valueOf(list.get(i)));
                                }
                            }
                        }
                        predicateDefinition.setName(name);
                        routeDefinition.getPredicates().add(predicateDefinition);
                    }
                }
                properties.getRoutes().add(routeDefinition);
            } catch (Exception e) {
                // ignore
            }
        }
        return properties;
    }

    @SuppressWarnings("ALL")
    public static Properties initProperties(java.util.Properties properties) {
        final Properties result = new Properties();
        properties = PropertiesUtil.matchProperties("p6e.cloud.gateway", properties);
        final java.util.Properties logProperties = PropertiesUtil.matchProperties("log", properties);
        final Boolean logEnable = PropertiesUtil.getBooleanProperty(logProperties, "enable");
        final Boolean logDetails = PropertiesUtil.getBooleanProperty(logProperties, "details");
        final List<Object> requestHeaderClear = PropertiesUtil.getListObjectProperty(properties, "requestHeaderClear");
        final List<Object> responseHeaderOnly = PropertiesUtil.getListObjectProperty(properties, "responseHeaderOnly");
        initBase(result, logEnable, logDetails, requestHeaderClear, responseHeaderOnly);
        return initPropertiesRoutes(PropertiesUtil.getListPropertiesProperty(properties, "routes"), result);
    }

    private static Properties initPropertiesRoutes(List<java.util.Properties> routes, Properties properties) {
        for (final java.util.Properties route : routes) {
            try {
                final RouteDefinition routeDefinition = new RouteDefinition();
                routeDefinition.setId(PropertiesUtil.getStringProperty(route, "id", GeneratorUtil.uuid()));
                routeDefinition.setOrder(PropertiesUtil.getIntegerProperty(route, "order", 0));
                routeDefinition.setUri(new URI(PropertiesUtil.getStringProperty(route, "uri")));
                routeDefinition.getMetadata().putAll(PropertiesUtil.getMapProperty(route, "metadata", new HashMap<>()));
                final List<java.util.Properties> filters = PropertiesUtil.getListPropertiesProperty(route, "filters");
                final List<java.util.Properties> predicates = PropertiesUtil.getListPropertiesProperty(route, "predicates");
                for (final java.util.Properties filter : filters) {
                    final FilterDefinition filterDefinition = new FilterDefinition();
                    final Map<String, Object> omap = PropertiesUtil.getMapProperty(filter, "args");
                    for (final String key : omap.keySet()) {
                        filterDefinition.getArgs().put(key, String.valueOf(omap.get(key)));
                    }
                    filterDefinition.setName(PropertiesUtil.getStringProperty(filter, "name"));
                    routeDefinition.getFilters().add(filterDefinition);
                }
                for (final java.util.Properties predicate : predicates) {
                    final PredicateDefinition predicateDefinition = new PredicateDefinition();
                    predicateDefinition.setName(PropertiesUtil.getStringProperty(predicate, "name"));
                    final Map<String, Object> omap = PropertiesUtil.getMapProperty(predicate, "args");
                    for (final String key : omap.keySet()) {
                        predicateDefinition.getArgs().put(initPropertiesRouteArgsKey(key), String.valueOf(omap.get(key)));
                    }
                    routeDefinition.getPredicates().add(predicateDefinition);
                }
                properties.getRoutes().add(routeDefinition);
            } catch (Exception e) {
                // ignore
            }
        }
        return properties;
    }

    private static void initBase(
            Properties properties,
            Boolean logEnable,
            Boolean logDetails,
            List<Object> requestHeaderClear,
            List<Object> responseHeaderOnly
    ) {
        if (logEnable != null) {
            properties.getLog().setEnable(logEnable);
        }
        if (logDetails != null) {
            properties.getLog().setDetails(logDetails);
        }
        if (requestHeaderClear != null) {
            final List<String> list = new ArrayList<>();
            for (Object item : requestHeaderClear) {
                list.add(TransformationUtil.objectToString(item));
            }
            properties.setRequestHeaderClear(list.toArray(new String[0]));
        }
        if (responseHeaderOnly != null) {
            final List<String> list = new ArrayList<>();
            for (Object item : responseHeaderOnly) {
                list.add(TransformationUtil.objectToString(item));
            }
            properties.setResponseHeaderOnly(list.toArray(new String[0]));
        }
    }

    private static String initPropertiesRouteArgsKey(String content) {
        StringBuilder num = null;
        int mark = content.length();
        for (int i = content.length() - 1; i >= 0; i--) {
            final String ch = String.valueOf(content.charAt(i));
            if (i + 1 == content.length() && "]".equals(ch)) {
                num = new StringBuilder();
            } else if ("[".equals(ch)) {
                mark = i;
                break;
            } else if (num != null) {
                num.insert(0, ch);
            }
        }
        return content.substring(0, mark) + (num == null ? "" : "." + num);
    }


    /**
     * Log
     */
    @Data
    @Accessors(chain = true)
    public static class Log implements Serializable {

        /**
         * 是否启动
         */
        private boolean enable = false;

        /**
         * 是否启动详细信息打印
         */
        private boolean details = false;

    }

    /**
     * Log
     */
    private Log log = new Log();

    /**
     * Request Header Clear
     */
    private String[] requestHeaderClear = new String[]{};

    /**
     * Response Header Only
     */
    private String[] responseHeaderOnly = new String[]{};

    /**
     * Route Definition List
     */
    private List<RouteDefinition> routes = new ArrayList<>();

}