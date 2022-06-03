package cn.gudqs7.plugins.common.util;

import java.util.HashSet;
import java.util.Set;

/**
 * @author wq
 */
public class ParamFilter {


    private static final Set<String> JUMP_FIELD_NAME_SET = new HashSet<>();
    private static final Set<String> JUMP_TYPE_NAME_SET = new HashSet<>();

    static {
        JUMP_FIELD_NAME_SET.add("serialVersionUID");
        JUMP_TYPE_NAME_SET.add("javax.servlet.ServletRequest");
        JUMP_TYPE_NAME_SET.add("javax.servlet.ServletResponse");
        JUMP_TYPE_NAME_SET.add("javax.servlet.http.HttpServletRequest");
        JUMP_TYPE_NAME_SET.add("javax.servlet.http.HttpServletResponse");
        JUMP_TYPE_NAME_SET.add("javax.servlet.http.HttpSession");
    }

    public static boolean isFieldNameNeedJump(String fieldName) {
        if (JUMP_FIELD_NAME_SET.contains(fieldName)) {
            return true;
        }
        return false;
    }

    public static boolean isFieldTypeNeedJump(String typeQname) {
        if (JUMP_TYPE_NAME_SET.contains(typeQname)) {
            return true;
        }
        return false;
    }

}
