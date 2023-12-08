package club.p6e.cloud.gateway;

import club.p6e.coat.common.utils.GeneratorUtil;
import club.p6e.coat.common.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lidashuang
 * @version 1.0
 */
@Component
@Configuration
public class PropertiesRefresher {

    /**
     * 注入日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesRefresher.class);

    /**
     * 配置文件对象
     */
    private final Properties properties;

    /**
     * 自定义路由定位器
     */
    private final CustomRouteLocator locator;

    /**
     * 构造方法初始化
     *
     * @param properties 配置文件对象
     * @param locator    路由初始化定位器
     */
    public PropertiesRefresher(Properties properties, CustomRouteLocator locator) {
        this.locator = locator;
        this.properties = properties;
        this.locator.refresh(this.properties.getRoutes());
    }

    public void execute(Map<String, Object> data) {
        if (data != null) {
            final String version = get(data, "p6e.cloud.gateway.version");
            if (version != null) {
                this.properties.setVersion(version);
            }
            final Object le = get(data, "p6e.cloud.gateway.log.enabled");
            if (le != null) {
                this.properties.getLog().setEnabled("true".equalsIgnoreCase(String.valueOf(le)));
            }
            final Object ld = get(data, "p6e.cloud.gateway.log.details");
            if (ld != null) {
                this.properties.getLog().setDetails("true".equalsIgnoreCase(String.valueOf(ld)));
            }
            final Object re = get(data, "p6e.cloud.gateway.referer.enabled");
            if (re != null) {
                this.properties.getReferer().setEnabled("true".equalsIgnoreCase(String.valueOf(re)));
            }
            final List<String> rw = get(data, "p6e.cloud.gateway.referer.white-list");
            if (rw != null) {
                this.properties.getReferer().setWhiteList(rw.toArray(new String[0]));
            }
            final Object ce = get(data, "p6e.cloud.gateway.cross-domain.enabled");
            if (ce != null) {
                this.properties.getCrossDomain().setEnabled("true".equalsIgnoreCase(String.valueOf(ce)));
            }
            final List<String> rc = get(data, "p6e.cloud.gateway.request-header-clear");
            if (rc != null) {
                this.properties.setRequestHeaderClear(rc);
            }
            final List<String> ro = get(data, "p6e.cloud.gateway.response-header-only");
            if (ro != null) {
                this.properties.setResponseHeaderOnly(ro);
            }
            final Object routes = get(data, "p6e.cloud.gateway.routes");
            if (routes != null) {
                this.locator.refresh(getRouteDefinition((List<?>) routes));
            }
        }
    }

    @SuppressWarnings("ALL")
    private <T> T get(Map<String, Object> data, String name) {
        if (data != null && name != null) {
            name = name.toLowerCase().replaceAll("-", "");
            if (mate(data, name) == null) {
                final String[] ns = name.split("\\.");
                for (int i = 0; i < ns.length; i++) {
                    final Object o = mate(data, ns[i]);
                    if (i + 1 == ns.length && o != null) {
                        return (T) o;
                    } else if (o instanceof final Map<?, ?> m) {
                        data = (Map<String, Object>) m;
                    } else {
                        return null;
                    }
                }
            } else {
                return (T) mate(data, name);
            }
        }
        return null;
    }

    private Object mate(Map<String, Object> data, String name) {
        if (data != null && name != null) {
            for (final String key : data.keySet()) {
                if (key.toLowerCase().replaceAll("-", "")
                        .equalsIgnoreCase(name.toLowerCase().replaceAll("-", ""))) {
                    return data.get(key);
                }
            }
        }
        return null;
    }

    @SuppressWarnings("ALL")
    private List<RouteDefinition> getRouteDefinition(List<?> routes) {
        final List<RouteDefinition> list = new ArrayList<>();
        if (routes != null) {
            for (final Object route : routes) {
                try {
                    if (route instanceof Map) {
                        final Map<String, Object> map = (Map<String, Object>) route;
                        final RouteDefinition definition = new RouteDefinition();
                        definition.setId(map.get("id") == null
                                ? GeneratorUtil.uuid() : String.valueOf(map.get("id")));
                        definition.setOrder(map.get("order") == null
                                ? 0 : Integer.parseInt(String.valueOf(map.get("order"))));
                        definition.setUri(new URI(String.valueOf(map.get("uri"))));
                        definition.setMetadata(map.get("metadata") == null
                                ? new HashMap<>() : (Map<String, Object>) map.get("metadata"));
                        if (map.get("filters") instanceof String) {
                            definition.setFilters();
                        }
                        if (map.get("predicates") instanceof String) {

                        }
                        list.add(definition);
                    }
                } catch (Exception e) {
                    LOGGER.error("[P6E GATEWAY ROUTES ERROR]", e);
                }
            }
        }
        return list;
    }

}
