package cn.gudqs7.plugins.savior.savior.more;

import cn.gudqs7.plugins.common.consts.MapKeyConstant;
import cn.gudqs7.plugins.common.enums.MoreCommentTagEnum;
import cn.gudqs7.plugins.common.pojo.resolver.CommentInfo;
import cn.gudqs7.plugins.common.pojo.resolver.RequestMapping;
import cn.gudqs7.plugins.common.pojo.resolver.StructureAndCommentInfo;
import cn.gudqs7.plugins.common.resolver.comment.AnnotationHolder;
import cn.gudqs7.plugins.common.util.JsonUtil;
import cn.gudqs7.plugins.common.util.file.FreeMarkerUtil;
import cn.gudqs7.plugins.savior.pojo.PostmanKvInfo;
import cn.gudqs7.plugins.savior.reader.Java2BulkReader;
import cn.gudqs7.plugins.savior.savior.base.AbstractSavior;
import cn.gudqs7.plugins.savior.theme.Theme;
import cn.gudqs7.plugins.savior.util.RestfulUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author wq
 * @date 2021/5/19
 */
public class JavaToPostmanSavior extends AbstractSavior<Map<String, Object>> {

    private final Java2BulkReader java2BulkReader;
    private final JavaToDocSavior javaToDocSavior;

    public JavaToPostmanSavior(Theme theme) {
        super(theme);
        java2BulkReader = new Java2BulkReader(theme);
        javaToDocSavior = new JavaToDocSavior(theme);
    }

    public Map<String, Object> generatePostmanItem(PsiClass psiClass, Project project) {
        AnnotationHolder psiClassHolder = AnnotationHolder.getPsiClassHolder(psiClass);
        CommentInfo commentInfo = psiClassHolder.getCommentInfo();
        String itemName = commentInfo.getItemName(psiClass.getName());
        boolean hidden = commentInfo.isHidden(false);
        if (hidden) {
            return null;
        }

        String hostAndPort = "";
        String interfaceClassName = psiClass.getQualifiedName();
        boolean noResponse = commentInfo.getSingleBool(MoreCommentTagEnum.POSTMAN_NO_RESPONSE.getTag(), false);
        List<Map<String, Object>> itemList = new ArrayList<>();

        List<PsiMethod> methods = getMethodList(psiClass);
        for (PsiMethod method : methods) {
            Map<String, Object> postmanRequestByMethod = generatePostmanRequestByMethod(project, interfaceClassName, method, noResponse);
            if (postmanRequestByMethod != null) {
                hostAndPort = postmanRequestByMethod.remove(MapKeyConstant.HOST_PORT).toString();
                itemList.add(postmanRequestByMethod);
            }
        }
        if (itemList.size() == 0) {
            return null;
        }
        if (itemList.size() == 1) {
            Map<String, Object> map = itemList.get(0);
            map.put(MapKeyConstant.HOST_PORT, hostAndPort);
            return map;
        }

        Map<String, Object> itemMap = new LinkedHashMap<>(8);
        itemMap.put("name", itemName);
        itemMap.put("item", itemList);
        itemMap.put(MapKeyConstant.HOST_PORT, hostAndPort);

        return itemMap;
    }

    public Map<String, Object> generatePostmanRequestByMethod(Project project, String interfaceClassName, PsiMethod publicMethod, boolean noResponse) {
        Map<String, Object> data = getDataByMethod(project, interfaceClassName, publicMethod, false);
        if (data == null) {
            return null;
        }
        if (noResponse) {
            data.remove("response");
        }
        return data;
    }

    @Override
    protected Map<String, Object> getDataByStructureAndCommentInfo(Project project, PsiMethod publicMethod, CommentInfo commentInfo, String interfaceClassName, StructureAndCommentInfo paramStructureAndCommentInfo, StructureAndCommentInfo returnStructureAndCommentInfo, Map<String, Object> param) {
        Map<String, Object> data = javaToDocSavior.getDataByStructureAndCommentInfo(project, publicMethod, commentInfo, interfaceClassName, paramStructureAndCommentInfo, returnStructureAndCommentInfo, param);
        String template = FreeMarkerUtil.renderTemplate(theme.getMethodPath(), data);

        String url = commentInfo.getUrl("");
        String contentType = commentInfo.getContentType(theme.getDefaultContentType());
        String methodName = publicMethod.getName();
        String interfaceName = commentInfo.getValue(methodName);
        Boolean noResponse = commentInfo.getSingleBool(MoreCommentTagEnum.POSTMAN_NO_RESPONSE.getTag(), false);
        String method = commentInfo.getMethod("");
        String method0 = RestfulUtil.getFirstMethod(method);
        boolean firstMethodIsGet = RequestMapping.Method.GET.equals(method0);

        String mode = null;
        Object modeContent = null;

        Map<String, Object> body = new LinkedHashMap<>(8);
        List<PostmanKvInfo> queryList = null;
        if (firstMethodIsGet) {
            queryList = getQueryList(paramStructureAndCommentInfo);
        } else {
            switch (contentType) {
                case RequestMapping.ContentType.APPLICATION_JSON:
                    mode = "raw";
                    modeContent = getRequestBodyJson(paramStructureAndCommentInfo);
                    queryList = getQueryList(paramStructureAndCommentInfo);
                    body.put("options", getRawOptions());
                    break;
                case RequestMapping.ContentType.FORM_DATA:
                    mode = "formdata";
                    modeContent = java2BulkReader.read(paramStructureAndCommentInfo);
                    break;
                case RequestMapping.ContentType.X_WWW_FORM_URLENCODED:
                    mode = "urlencoded";
                    modeContent = java2BulkReader.read(paramStructureAndCommentInfo);
                    break;
                default:
                    break;
            }
            if (mode != null) {
                body.put("mode", mode);
                body.put(mode, modeContent);
            }
        }

        Map<String, Object> urlMap = getUrlMap(url);
        if (CollectionUtils.isNotEmpty(queryList)) {
            urlMap.put("query", queryList);
        }

        String host = urlMap.remove("host").toString();
        String projectName = project.getName();
        urlMap.put("host", String.format("{{%s}}", projectName + "-url"));

        Map<String, Object> request = new LinkedHashMap<>(8);
        request.put("method", method0);
        request.put("header", new ArrayList<>(1));
        request.put("body", body);
        request.put("url", urlMap);
        request.put("description", template);

        Map<String, Object> postmanRequest = new LinkedHashMap<>(8);
        postmanRequest.put(MapKeyConstant.HOST_PORT, host);
        postmanRequest.put("name", interfaceName);
        postmanRequest.put("request", request);

        if (!noResponse) {
            Map<String, Object> returnJava2jsonMap = java2JsonReader.read(returnStructureAndCommentInfo);
            String returnJava2jsonStr = JsonUtil.toJson(returnJava2jsonMap);
            List<Map<String, Object>> responseList = Collections.singletonList(generatePostmanResponse(request, returnJava2jsonStr));
            postmanRequest.put("response", responseList);
        }
        return postmanRequest;
    }

    private List<PostmanKvInfo> getQueryList(StructureAndCommentInfo paramStructureAndCommentInfo) {
        HashMap<String, Object> data = new HashMap<>(2);
        data.put("removeRequestBody", true);
        return java2BulkReader.read(paramStructureAndCommentInfo, data);
    }

    @NotNull
    private Map<String, Object> getRawOptions() {
        Map<String, Object> raw = new LinkedHashMap<>(2);
        raw.put("language", "json");
        Map<String, Object> options = new LinkedHashMap<>(2);
        options.put("raw", raw);
        return options;
    }

    private String getRequestBodyJson(StructureAndCommentInfo paramStructureAndCommentInfo) {
        Map<String, Object> java2jsonMap = java2JsonReader.read(paramStructureAndCommentInfo);
        Object key = java2jsonMap.get(MapKeyConstant.HAS_REQUEST_BODY);
        if (key instanceof String) {
            String key0 = (String) key;
            return JsonUtil.toJson(java2jsonMap.get(key0));
        }
        return "";
    }

    private Map<String, Object> generatePostmanResponse(Map<String, Object> postmanRequest, String resultJava2json) {
        Map<String, Object> originalRequest = new LinkedHashMap<>(8);
        originalRequest.putAll(postmanRequest);
        originalRequest.remove("description");

        Map<String, Object> response = new LinkedHashMap<>(8);
        response.put("name", "返回示例");
        response.put("originalRequest", originalRequest);
        response.put("status", "OK");
        response.put("code", 200);
        response.put("_postman_previewlanguage", "json");
        response.put("header", new ArrayList<>());
        response.put("cookie", new ArrayList<>());
        response.put("body", resultJava2json);
        return response;
    }

    private Map<String, Object> getUrlMap(String url) {
        String urlCopy = "" + url;
        int indexOfProtocol = urlCopy.indexOf(":");
        String protocol = "http";
        String host = "";
        List<Object> pathList = new ArrayList<>();
        if (indexOfProtocol != -1) {
            protocol = urlCopy.substring(0, indexOfProtocol);
            urlCopy = urlCopy.substring(indexOfProtocol + 1);
        }
        if (urlCopy.startsWith("//")) {
            urlCopy = urlCopy.substring(2);
        }
        int indexOfAfterHost = urlCopy.indexOf("/");
        if (indexOfAfterHost != -1) {
            host = urlCopy.substring(0, indexOfAfterHost);
            urlCopy = urlCopy.substring(indexOfAfterHost + 1);
        }
        int indexOfParam = urlCopy.indexOf("?");
        String path = urlCopy;
        if (indexOfParam != -1) {
            path = urlCopy.substring(0, indexOfParam);
        }
        String[] pathArray = path.split("/");
        if (pathArray.length > 0) {
            pathList.addAll(Arrays.asList(pathArray));
        }
        Map<String, Object> urlMap = new LinkedHashMap<>(8);
        urlMap.put("protocol", protocol);
        urlMap.put("host", host);
        urlMap.put("path", pathList);
        return urlMap;
    }

}
