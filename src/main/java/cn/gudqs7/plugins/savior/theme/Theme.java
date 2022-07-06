package cn.gudqs7.plugins.savior.theme;

import cn.gudqs7.plugins.common.enums.PluginSettingEnum;
import cn.gudqs7.plugins.common.pojo.resolver.CommentInfo;
import cn.gudqs7.plugins.common.pojo.resolver.RequestMapping;
import cn.gudqs7.plugins.common.pojo.resolver.StructureAndCommentInfo;
import cn.gudqs7.plugins.common.resolver.comment.AnnotationHolder;
import cn.gudqs7.plugins.common.util.PluginSettingHelper;
import cn.gudqs7.plugins.savior.enums.ThemeType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
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
     * 获取模版路径前缀
     *
     * @return 模版路径前缀
     */
    String getPathPrefix();

    /**
     * 获取模版
     *
     * @return 方法模板
     */
    default String getMethodPath() {
        String pathPrefix = getPathPrefix();
        String methodTheme = PluginSettingHelper.getConfigItem(PluginSettingEnum.PREFIX_THEME_METHOD.getSettingKey() + pathPrefix);
        if (StringUtils.isNotBlank(methodTheme)) {
            String methodPath = pathPrefix + "/method-" + methodTheme + ".ftl";
            InputStream inputStream = Theme.class.getClassLoader().getResourceAsStream("template/ftl/" + methodPath);
            if (inputStream != null) {
                return methodPath;
            }
        }
        return pathPrefix + "/method.ftl";
    }

    /**
     * 获取模板
     *
     * @return 字段模板
     */
    default String getFieldPath() {
        String pathPrefix = getPathPrefix();
        String fieldTheme = PluginSettingHelper.getConfigItem(PluginSettingEnum.PREFIX_THEME_FIELD.getSettingKey() + pathPrefix);
        if (StringUtils.isNotBlank(fieldTheme)) {
            String fieldPath = pathPrefix + "/field-" + fieldTheme + ".ftl";
            InputStream inputStream = Theme.class.getClassLoader().getResourceAsStream("template/ftl/" + fieldPath);
            if (inputStream != null) {
                return fieldPath;
            }
        }
        return pathPrefix + "/field.ftl";
    }

    /**
     * 获取默认的 contentType
     *
     * @return 默认的 contentType
     */
    default String getDefaultContentType() {
        return RequestMapping.ContentType.APPLICATION_JSON;
    }

    /**
     * 判断是否跳过该方法
     *
     * @param annotationHolder 注释信息
     * @return 是否跳过该方法
     */
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
