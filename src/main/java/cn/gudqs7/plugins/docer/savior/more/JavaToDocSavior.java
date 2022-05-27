package cn.gudqs7.plugins.docer.savior.more;

import cn.gudqs7.plugins.docer.constant.MapKeyConstant;
import cn.gudqs7.plugins.docer.pojo.FieldLevelInfo;
import cn.gudqs7.plugins.docer.pojo.StructureAndCommentInfo;
import cn.gudqs7.plugins.docer.pojo.annotation.CommentInfo;
import cn.gudqs7.plugins.docer.pojo.annotation.ResponseCodeInfo;
import cn.gudqs7.plugins.docer.savior.base.AbstractSavior;
import cn.gudqs7.plugins.docer.theme.Theme;
import cn.gudqs7.plugins.docer.util.FreeMarkerUtil;
import cn.gudqs7.plugins.docer.util.JsonUtil;
import cn.gudqs7.plugins.util.PsiClassUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
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
        String template = FreeMarkerUtil.renderTemplate(theme.getMethodPath(), data);
        return Pair.of(template+"\n\n", apiName);
    }

    @Override
    protected Map<String, Object> getDataByStructureAndCommentInfo(
            Project project, PsiMethod publicMethod, CommentInfo commentInfo, String interfaceClassName,
            StructureAndCommentInfo paramStructureAndCommentInfo,
            StructureAndCommentInfo returnStructureAndCommentInfo,
            Map<String, Object> param) {
        Map<String, List<FieldLevelInfo>> paramLevelMap = java2ApiReader.read(paramStructureAndCommentInfo);
        Map<String, List<FieldLevelInfo>> returnLevelMap = java2ApiReader.read(returnStructureAndCommentInfo);
        Map<String, Object> java2jsonMap = java2JsonReader.read(paramStructureAndCommentInfo);
        Map<String, Object> returnJava2jsonMap = java2JsonReader.read(returnStructureAndCommentInfo);
        String java2jsonStr = JsonUtil.toJson(java2jsonMap);
        String returnJava2jsonStr = JsonUtil.toJson(returnJava2jsonMap.getOrDefault(MapKeyConstant.RETURN_FIELD_NAME, new Object()));

        Map<String, Object> dataByStr = collectDataByStr(project, publicMethod, commentInfo, interfaceClassName,
                paramLevelMap, returnLevelMap, java2jsonStr, returnJava2jsonStr);

        theme.afterCollectData(dataByStr, project, publicMethod, interfaceClassName, commentInfo,
                paramStructureAndCommentInfo, returnStructureAndCommentInfo,
                java2jsonMap, returnJava2jsonMap, java2jsonStr, returnJava2jsonStr);

        return dataByStr;
    }

    private Map<String, Object> collectDataByStr(
            Project project, PsiMethod publicMethod, CommentInfo commentInfo, String interfaceClassName,
            Map<String, List<FieldLevelInfo>> paramLevelMap, Map<String, List<FieldLevelInfo>> returnLevelMap, String java2jsonStr, String returnJava2jsonStr
    ) {
        String url = commentInfo.getUrl("");
        String contentType = commentInfo.getContentType(theme.getDefaultContentType());
        String method = commentInfo.getMethod("");
        String methodName = publicMethod.getName();
        String interfaceName = commentInfo.getValue(methodName);
        String notes = commentInfo.getNotes("");
        List<ResponseCodeInfo> responseCodeInfoList = commentInfo.getResponseCodeInfoList();

        Map<String, Object> data = new HashMap<>(16);
        data.put("interfaceName", interfaceName);
        data.put("interfaceNotes", notes);
        data.put("qualifiedMethodName", interfaceClassName + "#" + methodName);
        data.put("url", url);
        data.put("method", method);
        data.put("contentType", contentType);
        data.put("paramLevelMap", paramLevelMap);
        data.put("returnLevelMap", returnLevelMap);
        data.put("jsonExample", java2jsonStr);
        data.put("returnJsonExample", returnJava2jsonStr);
        data.put("responseCodeInfoList", responseCodeInfoList);
        return data;
    }

}
