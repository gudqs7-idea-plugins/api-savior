package cn.gudqs7.plugins.docer.savior.more;

import cn.gudqs7.plugins.docer.pojo.StructureAndCommentInfo;
import cn.gudqs7.plugins.docer.pojo.annotation.CommentInfo;
import cn.gudqs7.plugins.docer.pojo.annotation.RequestMapping;
import cn.gudqs7.plugins.docer.savior.base.AbstractSavior;
import cn.gudqs7.plugins.docer.theme.Theme;
import cn.gudqs7.plugins.docer.util.JsonUtil;
import cn.gudqs7.plugins.util.PsiClassUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * @author wq
 * @date 2021/5/19
 */
public class JavaToDocSavior extends AbstractSavior<Map<String, Object>> {

    public JavaToDocSavior(Theme theme) {
        super(theme);
    }

    public String generateApiByServiceInterface(PsiClass psiClass, Project project) {
        return generateApiByServiceInterfaceV2(psiClass, project).getLeft();
    }

    public Pair<String, List<String>> generateApiByServiceInterfaceV2(PsiClass psiClass, Project project) {
        String interfaceClassName = psiClass.getQualifiedName();
        PsiMethod[] methods = PsiClassUtil.getAllMethods(psiClass);
        // 根据 @Order 注解 以及字母顺序, 从小到大排序
        Arrays.sort(methods, this::orderByMethod);

        List<String> apiNameList = new ArrayList<>();
        StringBuilder allDoc = new StringBuilder();
        for (PsiMethod method : methods) {
            if (filterMethod(method)){
                continue;
            }

            Pair<String, String> pair = generateDocByMethodV2(project, interfaceClassName, method, false);
            String doc = pair.getLeft();
            allDoc.append(doc);
            String apiName = pair.getRight();
            if (apiName != null) {
                apiNameList.add(apiName);
            }
        }
        return Pair.of(allDoc.toString(), apiNameList);
    }

    public String generateDocByMethod(Project project, String interfaceClassName, PsiMethod publicMethod, boolean jumpHidden) {
        return generateDocByMethodV2(project, interfaceClassName, publicMethod, jumpHidden).getLeft();
    }

    public Pair<String, String> generateDocByMethodV2(Project project, String interfaceClassName, PsiMethod publicMethod, boolean jumpHidden) {
        Map<String, Object> data = getDataByMethod(project, interfaceClassName, publicMethod, jumpHidden);
        if (data == null) {
            return Pair.of("", null);
        }
        String apiName = data.getOrDefault("interfaceName", "").toString();
        String template = getTemplate(theme.getMethodPath(), data);
        return Pair.of(template+"\n\n", apiName);
    }

    @Override
    protected Map<String, Object> getDataByStructureAndCommentInfo(
            Project project, PsiMethod publicMethod, CommentInfo commentInfo, String interfaceClassName,
            StructureAndCommentInfo paramStructureAndCommentInfo,
            StructureAndCommentInfo returnStructureAndCommentInfo,
            Map<String, Object> param) {
        String java2apiDoc = java2ApiReader.read(paramStructureAndCommentInfo);
        Map<String, Object> java2jsonMap = java2JsonReader.read(paramStructureAndCommentInfo);
        String returnJava2apiDoc = java2ApiReader.read(returnStructureAndCommentInfo);
        Map<String, Object> returnJava2jsonMap = java2JsonReader.read(returnStructureAndCommentInfo);
        String java2jsonStr = JsonUtil.toJson(java2jsonMap);
        String returnJava2jsonStr = JsonUtil.toJson(returnJava2jsonMap);

        Map<String, Object> dataByStr = collectDataByStr(project, publicMethod, commentInfo, interfaceClassName,
                java2apiDoc, returnJava2apiDoc, java2jsonStr, returnJava2jsonStr);

        theme.afterCollectData(dataByStr, project, publicMethod, interfaceClassName, commentInfo,
                paramStructureAndCommentInfo, returnStructureAndCommentInfo,
                java2jsonMap, returnJava2jsonMap, java2jsonStr, returnJava2jsonStr);

        return dataByStr;
    }

    private Map<String, Object> collectDataByStr(
            Project project, PsiMethod publicMethod, CommentInfo commentInfo, String interfaceClassName,
            String java2apiDoc, String returnJava2apiDoc, String java2jsonStr, String returnJava2jsonStr
    ) {
        String url = commentInfo.getUrl("");
        String contentType = commentInfo.getContentType(theme.getDefaultContentType());
        String method = commentInfo.getMethod("");
        boolean methodIsGet = RequestMapping.Method.GET.equals(method);
        String methodName = publicMethod.getName();
        String interfaceName = commentInfo.getValue(methodName);
        String interfaceNotes = "";
        String notes = commentInfo.getNotes("");
        if (StringUtils.isNotBlank(notes)) {
            interfaceNotes = "\n> " + notes;
        }
        String codeMemo = getCodeMemo(project, commentInfo);

        Map<String, Object> data = new HashMap<>(16);
        data.put("interfaceName", interfaceName);
        data.put("interfaceNotes", interfaceNotes);
        data.put("qualifiedMethodName", interfaceClassName + "#" + methodName);
        data.put("url", url);
        data.put("method", method);
        String contentTypeMd = String.format("\n### 请求体类型\n```\n%s\n```", contentType);
        data.put("contentType", methodIsGet ? "" : contentTypeMd);
        data.put("codeMemo", codeMemo);

        boolean noParamMemo = StringUtils.isBlank(java2apiDoc);
        boolean noResultMemo = StringUtils.isBlank(returnJava2apiDoc);
        String paramMemoMd = "\n### 入参字段说明\n" + java2apiDoc;
        String resultMemoMd = "\n### 返回字段说明\n" + returnJava2apiDoc;
        if (StringUtils.isBlank(returnJava2jsonStr) || "{}".equals(returnJava2jsonStr)) {
            returnJava2jsonStr = "此接口无任何出参";
        }
        boolean usingRequestBody = RequestMapping.ContentType.APPLICATION_JSON.equals(contentType);
        data.put("jsonExampleType", usingRequestBody? "RequestBody": "Postman==> Bulk Edit");
        data.put("paramMemo", noParamMemo ? "" : paramMemoMd);
        data.put("resultMemo", noResultMemo ? "" : resultMemoMd);
        data.put("jsonExample", java2jsonStr);
        data.put("returnJsonExample", returnJava2jsonStr);
        return data;
    }

}
