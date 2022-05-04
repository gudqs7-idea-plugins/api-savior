package cn.gudqs7.plugins.docer.theme;

import cn.gudqs7.plugins.docer.annotation.AnnotationHolder;
import cn.gudqs7.plugins.docer.constant.ThemeType;
import cn.gudqs7.plugins.docer.pojo.StructureAndCommentInfo;
import cn.gudqs7.plugins.docer.pojo.annotation.CommentInfo;
import cn.gudqs7.plugins.docer.pojo.annotation.RequestMapping;
import cn.gudqs7.plugins.docer.util.ParamFilter;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;

import java.util.Map;

/**
 * @author wq
 */
public interface Theme {

    /**
     * 获取类型
     *
     * @return 类型
     */
    ThemeType getThemeType();

    /**
     * 获取模版
     *
     * @return 方法模板
     */
    String getMethodPath();

    /**
     * 获取模板
     * @return 字段模板
     */
    String getFieldPath();

    /**
     * 获取默认的 contentType
     *
     * @return 默认的 contentType
     */
    default String getDefaultContentType() {
        return RequestMapping.ContentType.APPLICATION_JSON;
    }

    /**
     * 判断参数是否需要跳过
     *
     * @param fieldName
     * @param psiFieldType
     * @param oldVal
     * @return
     */
    default Boolean handleHidden(String fieldName, PsiType psiFieldType, Boolean oldVal) {
        if (ParamFilter.isFieldNameNeedJump(fieldName)) {
            return true;
        }
        String typeQname = psiFieldType.getCanonicalText();
        if (ParamFilter.isFieldTypeNeedJump(typeQname)) {
            return true;
        }
        return oldVal;
    }

    default boolean handleMethodHidden(AnnotationHolder annotationHolder) {
        return false;
    }

    /**
     * 针对参数做一些处理, 如 @RequestBody, 如是否需要打散
     *
     * @param structureAndCommentInfo 结构+注释信息
     * @param map                     示例根 map
     * @param fieldName               当前节点名称
     * @return needBreakUpParam: 是否需要拆分param字段信息到外层, 如原来是 {param:{k1: v1, k2: v2}} 变成 {k1: v1, k2: v2}
     */
    default boolean handleParameter(StructureAndCommentInfo structureAndCommentInfo, Map<String, Object> map, String fieldName) {
        return false;
    }

    /**
     * 收集参数/返回值等数据的后置处理
     *
     * @param dataByStr                     最终数据map
     * @param project                       项目
     * @param publicMethod                  接口方法
     * @param interfaceClassName            接口类名
     * @param commentInfo                   接口相关注释/注解信息
     * @param paramStructureAndCommentInfo  参数相关信息
     * @param returnStructureAndCommentInfo 返回值相关信息
     * @param java2jsonMap                  参数示例值map
     * @param returnJava2jsonMap            返回值示例值map
     * @param java2jsonStr                  参数实例值json/bulk
     * @param returnJava2jsonStr            返回值示例值json
     */
    default void afterCollectData(Map<String, Object> dataByStr, Project project, PsiMethod publicMethod, String interfaceClassName, CommentInfo commentInfo, StructureAndCommentInfo paramStructureAndCommentInfo, StructureAndCommentInfo returnStructureAndCommentInfo, Map<String, Object> java2jsonMap, Map<String, Object> returnJava2jsonMap, String java2jsonStr, String returnJava2jsonStr) {

    }
}
