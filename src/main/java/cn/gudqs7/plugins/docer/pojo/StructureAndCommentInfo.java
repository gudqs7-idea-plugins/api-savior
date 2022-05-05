package cn.gudqs7.plugins.docer.pojo;

import cn.gudqs7.plugins.docer.constant.StructureType;
import cn.gudqs7.plugins.docer.pojo.annotation.CommentInfo;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import lombok.Data;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 结构及注释/注解信息
 *
 * @author WQ
 * @date 2022/4/4
 */
@Data
public class StructureAndCommentInfo {

    private StructureAndCommentInfo parent;
    private final Map<String, StructureAndCommentInfo> children = new LinkedHashMap<>(32);
    private final Map<String, Object> other = new HashMap<>(32);

    private boolean returnType = false;
    private boolean leaf = false;

    private Integer level;
    private String fieldName;
    private String fieldType;
    private String originalFieldType;

    private CommentInfo commentInfo;

    /**
     * 结构类型
     * 1.类
     * 2.字段
     * 3.参数
     * 4.返回值
     * 5.方法(暂时用不到)
     * 6.参数列表
     */
    private Integer type = 1;

    /**
     * 1:基础类型
     * 2:用户自定义类
     * 3:数组
     * 4:List(包括Set)
     * 5:Map
     * 6:参数列表
     * 7:返回值
     */
    private Integer fieldTypeCode = 1;
    private Integer originalFieldTypeCode = 1;

    private transient PsiClass psiClass;
    private transient PsiField psiField;
    private transient PsiParameter psiParameter;
    private transient PsiParameterList psiParameterList;
    private transient PsiTypeElement returnTypeElement;
    private transient PsiMethod psiMethod;

    public void copyOtherFromParent(Map<String, Object> other) {
        this.other.putAll(other);
    }

    public void addToOther(String key, Object val) {
        other.put(key, val);
    }

    public Object getOther(String key) {
        return other.get(key);
    }

    public Boolean getOtherBool(String key, Boolean defaultVal) {
        Object val = getOther(key);
        if (val instanceof Boolean) {
            return (Boolean) val;
        } else {
            return defaultVal;
        }
    }

    public String getOtherStr(String key, String defaultVal) {
        Object val = getOther(key);
        if (val instanceof String) {
            return (String) val;
        } else {
            return defaultVal;
        }
    }

    public Integer getOtherInt(String key, Integer defaultVal) {
        Object val = getOther(key);
        if (val instanceof Integer) {
            return (Integer) val;
        } else {
            return defaultVal;
        }
    }

    public void copyChild(Map<String, StructureAndCommentInfo> children) {
        if (children != null && children.size() > 0) {
            this.children.putAll(children);
        }
    }

    public void addChild(String fieldName, StructureAndCommentInfo structureAndCommentInfo) {
        structureAndCommentInfo.copyOtherFromParent(this.other);
        structureAndCommentInfo.setParent(this);
        children.put(fieldName, structureAndCommentInfo);
    }

    public Project getProject() {
        Project project = null;
        if (type != null) {
            switch (StructureType.of(type)) {
                case PSI_CLASS:
                    project = psiClass.getProject();
                    break;
                case PSI_FIELD:
                    project = psiField.getProject();
                    break;
                case PSI_PARAM:
                    project = psiParameter.getProject();
                    break;
                case PSI_PARAM_LIST:
                    project = psiParameterList.getProject();
                    break;
                case PSI_RETURN:
                    project = returnTypeElement.getProject();
                    break;
                default:
                    break;
            }
        }
        return project;
    }
}
