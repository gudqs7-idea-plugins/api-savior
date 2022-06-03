package cn.gudqs7.plugins.savior.docer.util;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wq
 */
public class DataHolder {

    private static final ConcurrentHashMap<String, Object> DATA = new ConcurrentHashMap<>(16);

    public static <T> void addData(String key, T data) {
        DATA.put(key, data);
    }

    public static void removeData(String key) {
        DATA.remove(key);
    }

    public static void removeAll() {
        DATA.clear();
    }

    public static <T> T getData(String key) {
        return (T) DATA.get(key);
    }


}
