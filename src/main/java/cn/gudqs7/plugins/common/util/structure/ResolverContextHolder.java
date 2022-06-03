package cn.gudqs7.plugins.common.util.structure;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wq
 */
public class ResolverContextHolder {

    public static final String HIDDEN_KEYS = "hiddenKeyList";
    public static final String ONLY_KEYS = "onlyKeyList";

    private static final ConcurrentHashMap<String, Object> CONTEXT_DATA = new ConcurrentHashMap<>(16);

    public static <T> void addData(String key, T data) {
        CONTEXT_DATA.put(key, data);
    }
    
    public static void removeAll() {
        CONTEXT_DATA.clear();
    }

    public static <T> T getData(String key) {
        return (T) CONTEXT_DATA.get(key);
    }


}
