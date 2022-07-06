package cn.gudqs7.plugins.common.util.structure;

/**
 * @author wq
 */
public class BaseTypeParseUtil {


    public static boolean parseBoolean(String value, boolean defaultVal) {
        try {
            return Boolean.parseBoolean(value);
        } catch (Exception ignored) {
            return defaultVal;
        }
    }

    public static Integer parseInt(String value, Integer defaultVal) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
            return defaultVal;
        }
    }
}
