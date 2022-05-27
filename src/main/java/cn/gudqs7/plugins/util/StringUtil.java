package cn.gudqs7.plugins.util;

import org.apache.commons.lang3.StringUtils;

/**
 * @author WQ
 */
public class StringUtil {

    public static String toCamelCase(String str) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        if (str.length() == 1) {
            return str.toLowerCase();
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

}
