package cn.gudqs7.plugins.docer.savior.base;

import cn.gudqs7.plugins.docer.annotation.AnnotationHolder;
import cn.gudqs7.plugins.docer.constant.MapKeyConstant;
import cn.gudqs7.plugins.docer.pojo.StructureAndCommentInfo;
import cn.gudqs7.plugins.docer.pojo.annotation.CommentInfo;
import cn.gudqs7.plugins.docer.pojo.annotation.ResponseCodeInfo;
import cn.gudqs7.plugins.docer.reader.Java2ApiReader;
import cn.gudqs7.plugins.docer.reader.Java2JsonReader;
import cn.gudqs7.plugins.docer.resolver.StructureAndCommentResolver;
import cn.gudqs7.plugins.docer.theme.Theme;
import cn.gudqs7.plugins.docer.util.DataHolder;
import cn.gudqs7.plugins.util.PsiUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wq
 * @date 2021/5/19
 */
public abstract class AbstractSavior<T> extends BaseSavior {

    protected final Java2JsonReader java2JsonReader;
    protected final Java2ApiReader java2ApiReader;
    protected final StructureAndCommentResolver structureAndCommentResolver;

    public AbstractSavior(Theme theme) {
        super(theme);
        java2JsonReader = new Java2JsonReader(theme);
        java2ApiReader = new Java2ApiReader(theme);
        structureAndCommentResolver = new StructureAndCommentResolver(theme);
    }

    protected int orderByMethod(PsiMethod publicMethod, PsiMethod publicMethod2) {
        PsiAnnotation orderAnnotation = publicMethod.getAnnotation("org.springframework.core.annotation.Order");
        int order = Integer.MAX_VALUE;
        if (orderAnnotation != null) {
            order = getAnnotationValue(orderAnnotation, "value", order);
        }
        PsiAnnotation orderAnnotation2 = publicMethod2.getAnnotation("org.springframework.core.annotation.Order");
        int order2 = Integer.MAX_VALUE;
        if (orderAnnotation2 != null) {
            order2 = getAnnotationValue(orderAnnotation2, "value", order2);
        }
        return order - order2;
    }

    protected boolean filterMethod(PsiMethod method) {
        if (method.isConstructor()) {
            return true;
        }
        PsiModifierList modifierList = method.getModifierList();
        if (modifierList.hasModifierProperty(PsiModifier.STATIC)) {
            return true;
        }
        if (modifierList.hasModifierProperty(PsiModifier.PRIVATE)) {
            return true;
        }
        // 最后排除 Lombok 生成的方法
        return "LombokLightModifierList".equals(modifierList.toString());
    }

    /**
     * 获取方法上的ActionName注释
     *
     * @param method 方法
     * @return 方法上的ActionName注释
     */
    protected String getMethodActionName(PsiMethod method) {
        AnnotationHolder psiMethodHolder = AnnotationHolder.getPsiMethodHolder(method);
        CommentInfo propertyForMethod = psiMethodHolder.getCommentInfo();
        if (propertyForMethod != null) {
            return propertyForMethod.getSingleStr("ActionName", null);
        }
        return null;
    }

    /**
     * 根据 PsiMethod 获取想要的数据
     *
     * @param project            项目
     * @param publicMethod       方法
     * @param interfaceClassName 接口类名
     * @param jumpHidden         是否跳过 hidden 检查
     * @return 想要的数据
     */
    protected T getDataByMethod(Project project, String interfaceClassName, PsiMethod publicMethod, boolean jumpHidden) {
        return getDataByMethod(project, interfaceClassName, publicMethod, new HashMap<>(2), jumpHidden);
    }


    /**
     * 根据 PsiMethod 获取想要的数据
     *
     * @param project            项目
     * @param publicMethod       方法
     * @param interfaceClassName 接口类名
     * @param param              配置参数
     * @param jumpHidden         是否跳过 hidden 检查
     * @return 想要的数据
     */
    protected T getDataByMethod(Project project, String interfaceClassName, PsiMethod publicMethod, Map<String, Object> param, boolean jumpHidden) {
        AnnotationHolder annotationHolder = AnnotationHolder.getPsiMethodHolder(publicMethod);
        CommentInfo commentInfo = annotationHolder.getCommentInfo();

        if (!jumpHidden) {
            boolean hidden = commentInfo.isHidden(false);
            if (hidden || theme.handleMethodHidden(annotationHolder)) {
                return null;
            }
        }
        structureAndCommentResolver.setProject(project);

        List<String> hiddenRequest = commentInfo.getHiddenRequest();
        List<String> onlyRequest = commentInfo.getOnlyRequest();
        DataHolder.addData(MapKeyConstant.HIDDEN_KEYS, hiddenRequest);
        DataHolder.addData(MapKeyConstant.ONLY_KEYS, onlyRequest);

        PsiParameterList parameterTypes = publicMethod.getParameterList();
        StructureAndCommentInfo paramStructureAndCommentInfo = structureAndCommentResolver.resolveFromParameterList(parameterTypes);

        DataHolder.removeAll();

        List<String> hiddenResponse = commentInfo.getHiddenResponse();
        List<String> onlyResponse = commentInfo.getOnlyResponse();
        DataHolder.addData(MapKeyConstant.HIDDEN_KEYS, hiddenResponse);
        DataHolder.addData(MapKeyConstant.ONLY_KEYS, onlyResponse);

        PsiTypeElement returnTypeElement = publicMethod.getReturnTypeElement();
        StructureAndCommentInfo returnStructureAndCommentInfo = structureAndCommentResolver.resolveFromReturnVal(returnTypeElement);

        DataHolder.removeAll();

        T data = getDataByStructureAndCommentInfo(
                project, publicMethod, commentInfo, interfaceClassName,
                paramStructureAndCommentInfo, returnStructureAndCommentInfo, param
        );

        PsiUtil.clearGeneric();
        return data;
    }

    /**
     * 根据参数/返回值信息获取想要的数据
     *
     * @param project                       项目
     * @param publicMethod                  方法
     * @param commentInfo                   方法注释/注解信息
     * @param interfaceClassName            接口类名
     * @param paramStructureAndCommentInfo  参数注释+结构信息
     * @param returnStructureAndCommentInfo 返回值注释+结构信息
     * @param param                         配置参数
     * @return 想要的数据
     */
    protected abstract T getDataByStructureAndCommentInfo(
            Project project, PsiMethod publicMethod, CommentInfo commentInfo, String interfaceClassName,
            StructureAndCommentInfo paramStructureAndCommentInfo,
            StructureAndCommentInfo returnStructureAndCommentInfo,
            Map<String, Object> param);

    protected String getCodeMemo(Project project, CommentInfo commentInfo) {
        String codeMemo = "";
        List<ResponseCodeInfo> defaultCodeInfoList = new ArrayList<>();
        boolean showCodeInfo = false;
        try {
            String projectName = project.getName();
            String codeInfos = System.getenv("DOCER_CODE_INFOS_" + projectName);
            if ("true".equals(codeInfos)) {
                showCodeInfo = true;
            }
            GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
            Gson gson = gsonBuilder.create();
            defaultCodeInfoList = gson.fromJson(codeInfos, new TypeToken<List<ResponseCodeInfo>>() {
            }.getType());
        } catch (Exception ignored) {
        }
        List<ResponseCodeInfo> responseCodeInfoList = commentInfo.getResponseCodeInfoList();
        if (CollectionUtils.isNotEmpty(defaultCodeInfoList)) {
            responseCodeInfoList.addAll(0, defaultCodeInfoList);
        }
        if (CollectionUtils.isNotEmpty(responseCodeInfoList)) {
            StringBuilder codeMemoSbf = new StringBuilder("\n" +
                    "## 更多信息\n" +
                    "### code 更多含义\n" +
                    "\n" +
                    "| Code | 含义 | 出现原因 |\n" +
                    "| -------- | -------- | -------- |\n");
            for (ResponseCodeInfo responseCodeInfo : responseCodeInfoList) {
                String code = responseCodeInfo.getCode();
                if (!StringUtils.isBlank(code)) {
                    code = replaceMd(code);
                }
                String message = responseCodeInfo.getMessage();
                if (!StringUtils.isBlank(message)) {
                    message = replaceMd(message);
                }
                String reason = responseCodeInfo.getReason();
                if (!StringUtils.isBlank(reason)) {
                    reason = replaceMd(reason);
                } else {
                    reason = "";
                }
                codeMemoSbf.append(String.format("| **%s** | %s | %s |\n", code, message, reason));
            }
            codeMemo = codeMemoSbf.toString();
        } else {
            // 若不限制出现, 则显示默认
            if (showCodeInfo) {
                codeMemo = "\n" +
                        "## 更多信息\n" +
                        "### code 更多含义\n" +
                        "\n" +
                        "> 待补充\n" +
                        "\n" +
                        "| Code | 含义 | 出现原因 |\n" +
                        "| -------- | -------- | -------- |\n" +
                        "| 1     | xxx     | xxx     |\n" +
                        "| 2     | xxx     | xxx     |";
            }
        }
        return codeMemo;
    }

}
