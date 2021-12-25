package cn.gudqs7.plugins.generate.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wenquan
 * @date 2021/9/30
 */
public class BaseTypeUtil {

    private static final Map<String, String> COMMON_DEFAULT_VAL_MAP = new HashMap<>();
    private static final Map<String, String> COMMON_DEFAULT_VAL_IMPORT_MAP = new HashMap<>();

    static {
        // 此处处理不能直接 new 的类型, 也就是接口, 常用的接口目前只想到三大集合
        COMMON_DEFAULT_VAL_MAP.put("java.util.List", "new ArrayList<>()");
        COMMON_DEFAULT_VAL_MAP.put("java.util.Map", "new HashMap<>()");
        COMMON_DEFAULT_VAL_MAP.put("java.util.Set", "new HashSet<>()");
        COMMON_DEFAULT_VAL_MAP.put("java.math.BigDecimal", "new BigDecimal(0)");

        COMMON_DEFAULT_VAL_IMPORT_MAP.put("java.util.List", "java.util.ArrayList");
        COMMON_DEFAULT_VAL_IMPORT_MAP.put("java.util.Map", "java.util.HashMap");
        COMMON_DEFAULT_VAL_IMPORT_MAP.put("java.util.Set", "java.util.HashSet");
        COMMON_DEFAULT_VAL_IMPORT_MAP.put("java.math.BigDecimal", "java.math.BigDecimal");
    }

    public static boolean isJavaBaseType(String typeName) {
        return getJavaBaseTypeDefaultVal(typeName) != null;
    }

    public static String getJavaBaseTypeDefaultVal(String typeName) {
        switch (typeName.toLowerCase()) {
            case "byte":
            case "int":
            case "integer":
            case "short":
                return "0";
            case "char":
            case "character":
                return "'0'";
            case "boolean":
                return "false";
            case "double":
                return "0D";
            case "float":
                return "0f";
            case "long":
                return "0L";
            case "string":
                return "\"\"";
            default:
                return null;
        }
    }

    public static String getCommonDefaultVal(String typeName) {
        return COMMON_DEFAULT_VAL_MAP.get(typeName);
    }
    public static String getCommonDefaultValImport(String key) {
        return COMMON_DEFAULT_VAL_IMPORT_MAP.get(key);
    }

}
