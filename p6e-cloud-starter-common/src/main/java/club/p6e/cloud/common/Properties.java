package club.p6e.cloud.common;

import club.p6e.coat.common.utils.PropertiesUtil;
import club.p6e.coat.common.utils.TransformationUtil;
import club.p6e.coat.common.utils.YamlUtil;
import club.p6e.coat.common.Properties.Security;
import club.p6e.coat.common.Properties.Snowflake;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
@ConfigurationProperties(prefix = "p6e.cloud.common")
@Component(value = "club.p6e.cloud.common.Properties")
public class Properties implements Serializable {

    private String version = "unknown";
    private Security security = new club.p6e.coat.common.Properties.Security();
    private club.p6e.coat.common.Properties.CrossDomain crossDomain = new club.p6e.coat.common.Properties.CrossDomain();
    private Map<String, club.p6e.coat.common.Properties.Snowflake> snowflake = new HashMap<>();

    private static void init(
            Properties properties,
            String version,
            Boolean securityEnable,
            List<Object> securityVouchers,
            Boolean crossDomainEnable,
            List<Object> crossDomainWhiteList,
            Map<String, Object> snowflake
    ) {
        if (version != null) {
            properties.setVersion(version);
        }
        if (securityEnable != null) {
            properties.getSecurity().setEnable(securityEnable);
        }
        if (securityVouchers != null) {
            final List<String> vouchers = new ArrayList<>();
            for (final Object item : securityVouchers) {
                vouchers.add(TransformationUtil.objectToString(item));
            }
            properties.getSecurity().setVouchers(vouchers);
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
        if (snowflake != null) {
            for (final Map.Entry<String, Object> entry : snowflake.entrySet()) {
                final Map<String, Object> map = TransformationUtil.objectToMap(entry.getValue());
                final Snowflake ns = new Snowflake();
                ns.setWorkerId(TransformationUtil.objectToInteger(map.get("workerId")));
                ns.setDataCenterId(TransformationUtil.objectToInteger(map.get("dataCenterId")));
                properties.getSnowflake().put(entry.getKey(), ns);
            }
        }
    }

    public static Properties initYaml(Object data) {
        final Properties result = new Properties();
        final Object config = YamlUtil.paths(data, "p6e.cloud.common");
        final Map<String, Object> cmap = TransformationUtil.objectToMap(config);
        final String version = TransformationUtil.objectToString(YamlUtil.paths(cmap, "version"));
        final Boolean securityEnable = TransformationUtil.objectToBoolean(YamlUtil.paths(cmap, "security.enable"));
        final List<Object> securityVouchers = TransformationUtil.objectToList(YamlUtil.paths(cmap, "security.vouchers"));
        final Boolean crossDomainEnable = TransformationUtil.objectToBoolean(YamlUtil.paths(cmap, "crossDomain.enable"));
        final List<Object> crossDomainWhiteList = TransformationUtil.objectToList(YamlUtil.paths(cmap, "crossDomain.whiteList"));
        final Map<String, Object> snowflake = TransformationUtil.objectToMap(YamlUtil.paths(cmap, "snowflake"));
        init(result, version, securityEnable, securityVouchers, crossDomainEnable, crossDomainWhiteList, snowflake);
        return result;
    }

    public static Properties initProperties(java.util.Properties properties) {
        final Properties result = new Properties();
        properties = PropertiesUtil.matchProperties("p6e.cloud.common", properties);
        final String version = PropertiesUtil.getStringProperty(properties, "version");
        final java.util.Properties securityProperties = PropertiesUtil.matchProperties("security", properties);
        final Boolean securityEnable = PropertiesUtil.getBooleanProperty(securityProperties, "enable");
        final List<Object> securityVouchers = PropertiesUtil.getListObjectProperty(securityProperties, "vouchers");
        final java.util.Properties crossDomainProperties = PropertiesUtil.matchProperties("crossDomain", properties);
        final Boolean crossDomainEnable = PropertiesUtil.getBooleanProperty(crossDomainProperties, "enable");
        final List<Object> crossDomainWhiteList = PropertiesUtil.getListObjectProperty(crossDomainProperties, "whiteList");
        final Map<String, Object> snowflake = PropertiesUtil.getMapProperty(properties, "snowflake");
        init(result, version, securityEnable, securityVouchers, crossDomainEnable, crossDomainWhiteList, snowflake);
        return result;
    }

}