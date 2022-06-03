package cn.gudqs7.plugins.common.util.structure;

import java.util.HashSet;
import java.util.Set;

/**
 * @author wq
 */
public class FieldJumpUtil {


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
        return JUMP_FIELD_NAME_SET.contains(fieldName);
    }

    public static boolean isFieldTypeNeedJump(String typeQname) {
        return JUMP_TYPE_NAME_SET.contains(typeQname);
    }

}
