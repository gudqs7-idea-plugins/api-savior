package cn.gudqs7.plugins.util;

import cn.gudqs7.plugins.docer.constant.CommentConst;
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

    public static String replaceMd(String source) {
        if (source == null) {
            return null;
        }
        source = source.replaceAll("\\$", "\\\\\\$");
        source = source.replaceAll("\\{", "\\\\{");
        source = source.replaceAll("\\}", "\\\\}");
        source = source.replaceAll("\\<", "\\\\<");
        source = source.replaceAll("\\>", "\\\\>");
        source = source.replaceAll("\\|", "\\\\|");
        source = source.replaceAll(CommentConst.BREAK_LINE, "<br>");
        return source;
    }

}
