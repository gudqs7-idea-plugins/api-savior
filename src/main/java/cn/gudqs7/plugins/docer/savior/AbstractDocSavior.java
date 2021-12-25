package cn.gudqs7.plugins.docer.savior;

import cn.gudqs7.plugins.docer.constant.MapKeyConstant;
import cn.gudqs7.plugins.docer.pojo.annotation.ApiModelProperty;
import cn.gudqs7.plugins.docer.pojo.annotation.RequestMapping;
import cn.gudqs7.plugins.docer.pojo.annotation.ResponseCodeInfo;
import cn.gudqs7.plugins.docer.theme.Theme;
import cn.gudqs7.plugins.docer.util.AnnotationHolder;
import cn.gudqs7.plugins.docer.util.DataHolder;
import cn.gudqs7.plugins.util.PsiUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wq
 * @date 2021/5/19
 */
public class AbstractDocSavior extends BaseSavior {

    protected final JavaToJsonSavior jsonSavior;
    protected final JavaToApiSavior apiSavior;

    public AbstractDocSavior(Theme theme) {
        super(theme);
        jsonSavior = new JavaToJsonSavior(theme);
        apiSavior = new JavaToApiSavior(theme);
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

    protected Map<String, String> getData(Project project, String interfaceClassName, PsiMethod publicMethod, boolean jumpHidden) {
        String methodName = publicMethod.getName();

        boolean hidden;
        String interfaceName = methodName;
        String interfaceNotes = "";
        String url = "";
        String method = "";
        String contentType = theme.getDefaultContentType();

        AnnotationHolder annotationHolder = AnnotationHolder.getPsiMethodHolder(publicMethod);
        ApiModelProperty apiModelProperty = annotationHolder.getApiModelProperty();

        if (!jumpHidden) {
            hidden = apiModelProperty.isHidden(false);
            if (hidden || theme.handleMethodHidden(annotationHolder)) {
                return null;
            }
        }

        interfaceName = apiModelProperty.getValue(interfaceName);
        String notes = apiModelProperty.getNotes("");
        if (StringUtils.isNotBlank(notes)) {
            interfaceNotes = "\n> " + notes;
        }
        url = apiModelProperty.getUrl(url);
        method = apiModelProperty.getMethod(method);
        contentType = apiModelProperty.getContentType(contentType);

        boolean methodIsGet = RequestMapping.Method.GET.equals(method);

        List<String> hiddenRequest = apiModelProperty.getHiddenRequest();
        List<String> hiddenResponse = apiModelProperty.getHiddenResponse();
        List<String> onlyRequest = apiModelProperty.getOnlyRequest();
        List<String> onlyResponse = apiModelProperty.getOnlyResponse();
        DataHolder.addData(MapKeyConstant.HIDDEN_KEYS, hiddenRequest);
        DataHolder.addData(MapKeyConstant.ONLY_KEYS, onlyRequest);

        PsiParameterList parameterTypes = publicMethod.getParameterList();
        String java2api;
        Map<String, Object> java2json;
        boolean singleParam = false;
        if (parameterTypes.getParametersCount() == 1) {
            PsiParameter parameter = parameterTypes.getParameters()[0];
            java2api = apiSavior.parameterOnlyJava2api(parameter, project);
            java2json = jsonSavior.parameterOnlyJava2json(parameter, project);
            singleParam = true;
        } else {
            java2api = apiSavior.parameterListOnlyJava2api(parameterTypes, project);
            java2json = jsonSavior.parameterListOnlyJava2json(parameterTypes, project);
        }
        
        DataHolder.addData(MapKeyConstant.HIDDEN_KEYS, hiddenResponse);
        DataHolder.addData(MapKeyConstant.ONLY_KEYS, onlyResponse);

        PsiType returnType = publicMethod.getReturnType();
        PsiTypeElement returnTypeElement = publicMethod.getReturnTypeElement();
        String resultJava2api = apiSavior.returnOnlyJava2api(returnTypeElement, project);
        String resultJava2json = jsonSavior.returnOnlyJava2json(publicMethod, returnType, project);
        DataHolder.removeAll();

        String codeMemo = getCodeMemo(project, apiModelProperty);

        String qualifiedMethodName = interfaceClassName + "#" + methodName;
        Map<String, String> data = new HashMap<>(16);
        // 接口名
        data.put("interfaceName", interfaceName);
        data.put("interfaceNotes", interfaceNotes);
        // 方法全限定名
        data.put("qualifiedMethodName", qualifiedMethodName);
        // json 示例
        data.put("jsonExample", "");
        // 参数说明
        data.put("paramMemo", java2api);
        data.put("resultMemo", resultJava2api);
        data.put("returnJsonExample", resultJava2json);
        data.put("url", url);
        data.put("method", method);
        data.put("contentType", methodIsGet ? "" : contentType);
        data.put("codeMemo", codeMemo);
        data.put("noResponse", String.valueOf(apiModelProperty.getSingleBool("noResponse", null)));

        String method0 = getFirstMethod(method);
        boolean firstMethodIsGet = RequestMapping.Method.GET.equals(method0);
        boolean paramHasRequestBody = RequestMapping.ContentType.APPLICATION_JSON.equals(contentType);
        theme.handleJsonExampleAndUrl(java2json, singleParam, paramHasRequestBody, firstMethodIsGet, data, url);
        PsiUtil.clearGeneric();
        return data;
    }

    @NotNull
    protected String getFirstMethod(String method) {
        String method0 = method;
        if (method0.contains("/")) {
            method0 = method0.substring(0, method0.indexOf("/"));
        }
        return method0;
    }

    protected String getCodeMemo(Project project, ApiModelProperty apiModelProperty) {
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
        List<ResponseCodeInfo> responseCodeInfoList = apiModelProperty.getResponseCodeInfoList();
        if (!CollectionUtils.isEmpty(defaultCodeInfoList)) {
            responseCodeInfoList.addAll(0, defaultCodeInfoList);
        }
        if (!CollectionUtils.isEmpty(responseCodeInfoList)) {
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
