package cn.gudqs7.plugins.idea.savior;

import cn.gudqs7.plugins.idea.constant.MapKeyConstant;
import cn.gudqs7.plugins.idea.pojo.annotation.ApiModelProperty;
import cn.gudqs7.plugins.idea.pojo.annotation.RequestMapping;
import cn.gudqs7.plugins.idea.theme.Theme;
import cn.gudqs7.plugins.idea.util.AnnotationHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * @author wq
 * @date 2021/5/19
 */
public class JavaToPostmanSavior extends AbstractDocSavior {

    public JavaToPostmanSavior(Theme theme) {
        super(theme);
    }

    public Map<String, Object> generatePostmanItem(PsiClass psiClass, Project project) throws IOException {
        AnnotationHolder psiClassHolder = AnnotationHolder.getPsiClassHolder(psiClass);
        ApiModelProperty apiModelProperty = psiClassHolder.getApiModelProperty();
        String value = apiModelProperty.getValue(psiClass.getName());
        boolean hidden = apiModelProperty.isHidden(false);
        if (hidden) {
            return null;
        }

        String interfaceClassName = psiClass.getQualifiedName();
        PsiMethod[] methods = getAllMethods(psiClass);
        // 根据 @Order 注解 以及字母顺序, 从小到大排序
        Arrays.sort(methods, this::orderByMethod);

        List<Map<String, Object>> itemList = new ArrayList<>();

        String hostAndPort = "";
        for (PsiMethod method : methods) {
            if (this.filterMethod(method)) {
                continue;
            }

            Map<String, Object> postmanRequestByMethod = generatePostmanRequestByMethod(project, interfaceClassName, method);
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
        itemMap.put("name", value);
        itemMap.put("item", itemList);
        itemMap.put(MapKeyConstant.HOST_PORT, hostAndPort);

        return itemMap;
    }

    public Map<String, Object> generatePostmanRequestByMethod(Project project, String interfaceClassName, PsiMethod publicMethod) throws IOException {
        Map<String, String> data = getData(project, interfaceClassName, publicMethod);
        if (data == null) {
            return null;
        }
        String method = data.get("method");
        String contentType = data.get("contentType");
        String interfaceName = data.get("interfaceName");
        String returnJsonExample = data.get("returnJsonExample");

        String method0 = getFirstMethod(method);
        boolean paramHasRequestBody = RequestMapping.ContentType.APPLICATION_JSON.equals(contentType);

        String template = getTemplate(theme.getMethodPath(), data);

        String mode = paramHasRequestBody ? "raw" : "urlencoded";
        Object modeContent;
        String jsonExample = data.get("jsonExample");
        if (paramHasRequestBody) {
            modeContent = jsonExample;
        } else {
            modeContent = convertBulkToKeyValJson(jsonExample);
        }
        Map<String, Object> body = new LinkedHashMap<>(8);
        body.put("mode", mode);
        if (modeContent != null) {
            body.put(mode, modeContent);
        }
        if (paramHasRequestBody) {
            Map<String, Object> raw = new LinkedHashMap<>(2);
            raw.put("language", "json");
            Map<String, Object> options = new LinkedHashMap<>(2);
            options.put("raw", raw);
            body.put("options", options);
        }
        Map<String, Object> urlMap = getUrlMap(data);
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
        postmanRequest.put("name", interfaceName);
        postmanRequest.put("request", request);
        List<Map<String, Object>> responseList = Collections.singletonList(generatePostmanResponse(request, returnJsonExample));
        postmanRequest.put("response", responseList);
        postmanRequest.put(MapKeyConstant.HOST_PORT, host);

        return postmanRequest;
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

    private Map<String, Object> getUrlMap(Map<String, String> data) {
        String urlCopy = data.get("url");
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
        if (pathArray != null && pathArray.length > 0) {
            for (String path0 : pathArray) {
                pathList.add(path0);
            }
        }
        String paramExample = data.get("paramExample");
        List<Map<String, String>> queryList = convertBulkToKeyValJson(paramExample);
        Map<String, Object> urlMap = new LinkedHashMap<>(8);
        urlMap.put("protocol", protocol);
        urlMap.put("host", host);
        urlMap.put("path", pathList);
        if (queryList != null) {
            urlMap.put("query", queryList);
        }

        return urlMap;
    }

    private List<Map<String, String>> convertBulkToKeyValJson(String jsonExample) {
        if (StringUtils.isBlank(jsonExample)) {
            return null;
        }
        List<Map<String, String>> kvList = new ArrayList<>();
        String[] lineArray = jsonExample.split("\n");
        for (String line : lineArray) {
            if (StringUtils.isBlank(line)) {
                continue;
            }
            int indexOf = line.indexOf(":");
            if (indexOf != -1) {
                String key = line.substring(0, indexOf);
                String value = line.substring(indexOf + 1);
                if (StringUtils.isNotBlank(value)) {
                    value = value.replaceAll("&br;", "<br>");
                }
                Map<String, String> kvMap = new LinkedHashMap<>();
                kvMap.put("key", key);
                kvMap.put("value", value);
                kvMap.put("type", "text");
                kvList.add(kvMap);
            }
        }
        return kvList;
    }

}
