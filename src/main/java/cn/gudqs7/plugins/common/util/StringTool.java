package cn.gudqs7.plugins.common.util;

import cn.gudqs7.plugins.common.consts.CommonConst;
import org.apache.commons.lang3.StringUtils;

/**
 * @author WQ
 */
public class StringTool {

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
        source = source.replaceAll(CommonConst.BREAK_LINE, "<br/>");
        return source;
    }

    /**
     * 将字符串中正则表达式的的关键字进行转义
     * @param source 字符串
     * @return 转义后的字符串
     */
    public static String escapeRegex(String source) {
        return source.replaceAll("([${}()*+.\\[\\]?\\\\^|'\"])", "\\\\$1");
    }

    /**
     * 用于移除或转义 markdown 瞄点语法不支持的符号, 如空格转义为 -, ()直接移除, todo 待完善
     * @param source 瞄点
     * @return 符合 markdown 瞄点语法的瞄点
     */
    public static String removeRegex(String source) {
        source = source.toLowerCase();
        source = source.replaceAll(" ", "-");
        return source.replaceAll("([${}()*+.\\[\\]?\\\\^|'\"])", "");
    }

    public static String lineToCamelCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        StringBuilder result = new StringBuilder();
        boolean nextUpperCase = false;

        for (int i = 0; i < str.length(); i++) {
            char currentChar = str.charAt(i);

            if (currentChar == '_') {
                // 遇到下划线，标记下一个字符需要大写
                nextUpperCase = true;
            } else {
                if (nextUpperCase) {
                    // 当前字符需要大写（驼峰位置）
                    result.append(Character.toUpperCase(currentChar));
                    nextUpperCase = false;
                } else {
                    // 第一个单词保持小写，后续单词首字母已在上面的逻辑中处理
                    if (result.isEmpty()) {
                        // 第一个字符保持小写
                        result.append(Character.toLowerCase(currentChar));
                    } else {
                        result.append(currentChar);
                    }
                }
            }
        }

        return result.toString();
    }

}
