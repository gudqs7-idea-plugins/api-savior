package cn.gudqs7.plugins.common.pojo.resolver;

import cn.gudqs7.plugins.common.enums.StructureType;
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

    /**
     * 父级
     */
    private StructureAndCommentInfo parent;

    /**
     * 子元素
     */
    private final Map<String, StructureAndCommentInfo> children = new LinkedHashMap<>(32);

    /**
     * 其他信息
     */
    private final Map<String, Object> other = new HashMap<>(32);

    /**
     * 是否为返回类型
     */
    private boolean returnType = false;

    /**
     * 是否叶子节点
     */
    private boolean leaf = false;

    /**
     * 当前层级
     */
    private Integer level;

    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 字段类型(缩写)
     */
    private String fieldType;

    /**
     * 字段原始类型
     */
    private String originalFieldType;

    /**
     * 注释/注解信息
     */
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

    /**
     * 原始类型
     */
    private Integer originalFieldTypeCode = 1;

    /**
     * 类型信息
     */
    private PsiType psiType;

    /**
     * 相关信息
     */
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

    @Override
    public String toString() {
        return "StructureAndCommentInfo{" +
                "children=" + children +
                ", other=" + other +
                ", returnType=" + returnType +
                ", leaf=" + leaf +
                ", level=" + level +
                ", fieldName='" + fieldName + '\'' +
                ", fieldType='" + fieldType + '\'' +
                ", originalFieldType='" + originalFieldType + '\'' +
                ", commentInfo=" + commentInfo +
                ", type=" + type +
                ", fieldTypeCode=" + fieldTypeCode +
                ", originalFieldTypeCode=" + originalFieldTypeCode +
                ", psiType=" + psiType +
                '}';
    }
}
