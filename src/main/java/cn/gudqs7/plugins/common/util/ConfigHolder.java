package cn.gudqs7.plugins.common.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wq
 */
public class ConfigHolder {

    private static final Map<String, String> CONFIG = new ConcurrentHashMap<>(16);

    public static void putConfig(Map<String, String> config) {
        if (config != null) {
            CONFIG.putAll(config);
        }
    }

    public static void removeConfig() {
        CONFIG.clear();
    }

    public static Map<String, String> getConfig() {
        return CONFIG;
    }


}
