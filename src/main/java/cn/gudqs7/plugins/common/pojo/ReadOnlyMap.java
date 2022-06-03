package cn.gudqs7.plugins.common.pojo;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wenquan
 * @date 2022/5/5
 */
public class ReadOnlyMap {

    private final Map<String, Object> readOnlyData;

    public ReadOnlyMap() {
        this(new HashMap<>(2));
    }

    public ReadOnlyMap(Map<String, Object> readOnlyData) {
        this.readOnlyData = readOnlyData;
    }

    public <V> V get(String key, V defaultVal) {
        try {
            if (readOnlyData.size() > 0) {
                Object val = readOnlyData.get(key);
                if (val != null) {
                    return (V) val;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("获取数据失败, key=" + key + " , data=" + readOnlyData);
        }
        if (defaultVal != null) {
            return defaultVal;
        }
        throw new RuntimeException("数据为空, key=" + key + " , data=" + readOnlyData);
    }


    public static ReadOnlyMap of(Map<String, Object> readOnlyData) {
        return new ReadOnlyMap(readOnlyData);
    }

}
