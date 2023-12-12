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
 * 配置文件
 *
 * @author lidashuang
 * @version 1.0
 */
@Data
@Component
@Accessors(chain = true)
@ConfigurationProperties(prefix = "p6e.cloud.gateway")
public class Properties implements Serializable {

    /**
     * 版本号
     */
    private String version = "unknown";

    /**
     * Log
     */
    private Log log = new Log();

    /**
     * Referer
     */
    private Referer referer = new Referer();

    /**
     * Cross Domain
     */
    private CrossDomain crossDomain = new CrossDomain();

    /**
     * 请求头清除
     */
    private List<String> requestHeaderClear = new ArrayList<>();

    /**
     * 返回头唯一
     */
    private List<String> responseHeaderOnly = new ArrayList<>();

    /**
     * 路由
     */
    private List<RouteDefinition> routes = new ArrayList<>();

    public static Properties initYaml(Object data) {
        final Properties result = new Properties();
        final Object config = YamlUtil.paths(data, "p6e.cloud.gateway");
        final Map<String, Object> cmap = TransformationUtil.objectToMap(config);
        final String version = TransformationUtil.objectToString(YamlUtil.paths(cmap, "version"));
        final Boolean logEnabled = TransformationUtil.objectToBoolean(YamlUtil.paths(cmap, "log.enabled"));
        final Boolean logDetails = TransformationUtil.objectToBoolean(YamlUtil.paths(cmap, "log.details"));
        final Boolean refererEnabled = TransformationUtil.objectToBoolean(YamlUtil.paths(cmap, "referer.enabled"));
        final List<Object> refererWhiteList = TransformationUtil.objectToList(YamlUtil.paths(cmap, "referer.whiteList"));
        final Boolean crossDomainEnabled = TransformationUtil.objectToBoolean(YamlUtil.paths(cmap, "crossDomain.enabled"));
        final List<Object> requestHeaderClear = TransformationUtil.objectToList(YamlUtil.paths(cmap, "requestHeaderClear"));
        final List<Object> responseHeaderOnly = TransformationUtil.objectToList(YamlUtil.paths(cmap, "responseHeaderOnly"));
        if (version != null) {
            result.setVersion(version);
        }
        if (logEnabled != null) {
            result.getLog().setEnabled(logEnabled);
        }
        if (logDetails != null) {
            result.getLog().setDetails(logDetails);
        }
        if (refererEnabled != null) {
            result.getReferer().setEnabled(refererEnabled);
        }
        if (refererWhiteList != null) {
            final List<String> tmpList = new ArrayList<>();
            for (Object item : refererWhiteList) {
                tmpList.add(TransformationUtil.objectToString(item));
            }
            result.getReferer().setWhiteList(tmpList.toArray(new String[0]));
        }
        if (crossDomainEnabled != null) {
            result.getCrossDomain().setEnabled(crossDomainEnabled);
        }
        if (requestHeaderClear != null) {
            final List<String> tmpList = new ArrayList<>();
            for (Object item : requestHeaderClear) {
                tmpList.add(TransformationUtil.objectToString(item));
            }
            result.getRequestHeaderClear().addAll(tmpList);
        }
        if (responseHeaderOnly != null) {
            final List<String> tmpList = new ArrayList<>();
            for (Object item : responseHeaderOnly) {
                tmpList.add(TransformationUtil.objectToString(item));
            }
            result.getResponseHeaderOnly().addAll(tmpList);
        }
        final List<Object> routes = TransformationUtil.objectToList(YamlUtil.paths(cmap, "routes"));
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
                result.getRoutes().add(routeDefinition);
            } catch (Exception e) {
                // ignore
            }
        }
        return result;
    }

    public static Properties initProperties(java.util.Properties properties) {
        final Properties result = new Properties();
        properties = PropertiesUtil.matchProperties("p6e.cloud.gateway", properties);
        result.setVersion(PropertiesUtil.getStringProperty(properties, "version", "unknown"));
        final java.util.Properties logProperties = PropertiesUtil.matchProperties("log", properties);
        result.getLog().setEnabled(PropertiesUtil.getBooleanProperty(logProperties, "enabled", false));
        result.getLog().setDetails(PropertiesUtil.getBooleanProperty(logProperties, "details", false));
        final java.util.Properties refererProperties = PropertiesUtil.matchProperties("referer", properties);
        result.getReferer().setEnabled(PropertiesUtil.getBooleanProperty(refererProperties, "enabled", false));
        result.getReferer().setWhiteList(PropertiesUtil.getListStringProperty(
                refererProperties, "whiteList", List.of("*")).toArray(new String[0]));
        final java.util.Properties crossDomainProperties = PropertiesUtil.matchProperties("crossDomain", properties);
        result.getCrossDomain().setEnabled(PropertiesUtil.getBooleanProperty(crossDomainProperties, "enabled", false));
        result.getRequestHeaderClear().addAll(PropertiesUtil.getListStringProperty(properties, "requestHeaderClear", new ArrayList<>()));
        result.getResponseHeaderOnly().addAll(PropertiesUtil.getListStringProperty(properties, "responseHeaderOnly", new ArrayList<>()));
        final List<java.util.Properties> routes = PropertiesUtil.getListPropertiesProperty(properties, "routes");
        for (final java.util.Properties route : routes) {
            try {
                final RouteDefinition routeDefinition = new RouteDefinition();
                routeDefinition.setId(PropertiesUtil.getStringProperty(route, "id", GeneratorUtil.uuid()));
                routeDefinition.setOrder(PropertiesUtil.getIntegerProperty(route, "order", 0));
                routeDefinition.setUri(new URI(PropertiesUtil.getStringProperty(route, "uri")));
                System.out.println(PropertiesUtil.getMapProperty(route, "metadata", new HashMap<>()));
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
                        predicateDefinition.getArgs().put(key, String.valueOf(omap.get(key)));
                    }
                    routeDefinition.getPredicates().add(predicateDefinition);
                }
                result.getRoutes().add(routeDefinition);
            } catch (Exception e) {
                // ignore
            }
        }
        return result;
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
        private boolean enabled = false;

        /**
         * 是否启动打印
         */
        private boolean details = false;
    }

    /**
     * Referer
     */
    @Data
    @Accessors(chain = true)
    public static class Referer implements Serializable {
        /**
         * 是否启动
         */
        private boolean enabled = false;

        /**
         * 白名单
         */
        private String[] whiteList = new String[]{"*"};
    }

    /**
     * Cross Domain
     */
    @Data
    @Accessors(chain = true)
    public static class CrossDomain implements Serializable {
        /**
         * 是否启动
         */
        private boolean enabled = false;
    }

}