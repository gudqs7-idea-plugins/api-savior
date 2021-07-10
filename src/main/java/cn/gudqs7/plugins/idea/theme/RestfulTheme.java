package cn.gudqs7.plugins.idea.theme;

import cn.gudqs7.plugins.idea.constant.MapKeyConstant;
import cn.gudqs7.plugins.idea.pojo.ParamInfo;
import cn.gudqs7.plugins.idea.pojo.ParamLineInfo;
import cn.gudqs7.plugins.idea.pojo.annotation.ApiModelProperty;
import cn.gudqs7.plugins.idea.pojo.annotation.RequestMapping;
import cn.gudqs7.plugins.idea.savior.BaseSavior;
import cn.gudqs7.plugins.idea.util.AnnotationHolder;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiType;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wq
 */
public class RestfulTheme implements Theme {

    private static RestfulTheme instance;
    private final String FINAL_DEFAULT_OBJECT_EXAMPLE = "{}";

    public RestfulTheme() {
    }

    public static Theme getInstance() {
        if (instance == null) {
            synchronized (RestfulTheme.class) {
                if (instance == null) {
                    instance = new RestfulTheme();
                }
            }
        }
        return instance;
    }

    @Override
    public String getMethodPath() {
        return "template/restful/Method.txt";
    }

    @Override
    public String getParamContentPath(boolean returnParam) {
        if (returnParam) {
            return "template/restful/ReturnParamContent.txt";
        }
        return "template/restful/ParamContent.txt";
    }

    @Override
    public String getParamTitlePath(boolean returnParam) {
        if (returnParam) {
            return "template/restful/ReturnParamTitle.txt";
        }
        return "template/restful/ParamTitle.txt";
    }

    @Override
    public String printByGoMap(Map<Integer, List<ParamInfo>> goMap, boolean returnParam) {
        StringBuilder all = new StringBuilder();
        Map<Integer, ParamLineInfo> lineInfoMap = new TreeMap<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }
        });

        for (Map.Entry<Integer, List<ParamInfo>> entry : goMap.entrySet()) {
            Integer key = entry.getKey();
            List<ParamInfo> paramInfoList = entry.getValue();
            for (ParamInfo val : paramInfoList) {
                List<ParamLineInfo> allFields = val.getAllFields();
                for (ParamLineInfo paramLineInfo : allFields) {
                    lineInfoMap.put(paramLineInfo.getIndex(), paramLineInfo);
                }
            }
        }

        Map<String, String> data = new HashMap<>(16);
        data.put("fieldName", "字段");
        data.put("fieldType", "类型");
        data.put("required", "必填性");
        data.put("fieldDesc", "含义");
        data.put("notes", "其他信息参考");
        String allFields = lineInfoMap.values().stream().filter(paramLineInfo -> paramLineInfo.getLevel() > 0).map(ParamLineInfo::getLine).collect(Collectors.joining());
        data.put("allFields", allFields);
        if (StringUtils.isBlank(allFields)) {
            return handleNoField(returnParam);
        }
        return BaseSavior.getTemplate(getParamTitlePath(returnParam), data);
    }

    @Override
    public String handleNoField(boolean returnParam) {
        return "无任何字段";
    }

    @Override
    public String getDefaultContentType() {
        return RequestMapping.ContentType.FORM_DATA;
    }

    @Override
    public void handleParameterList(PsiParameter parameter, AnnotationHolder fieldAnnotation, Map<String, Object> map) {
        ApiModelProperty apiModelProperty = fieldAnnotation.getApiModelProperty();
        String fieldName = parameter.getName();
        fieldName = apiModelProperty.getName(fieldName);
        boolean hasRequestBody = fieldAnnotation.hasAnnotatation(AnnotationHolder.QNAME_OF_REQUEST_BODY);
        if (hasRequestBody) {
            map.put(MapKeyConstant.HAS_REQUEST_BODY, fieldName);
            return;
        }
        PsiType psiFieldType = parameter.getType();
        String typeName = psiFieldType.getPresentableText();
        if (!BaseSavior.isJavaBaseType(typeName)) {
            Object obj = map.remove(fieldName);
            if (obj instanceof Map) {
                Map paramMap = (Map) obj;
                if (paramMap != null && paramMap.size() > 0) {
                    map.putAll(paramMap);
                    return;
                }
            }
            map.put(fieldName, obj);
        }
    }

    @Override
    public String formatJson(Object value, String contentType) {
        if (contentType == null || RequestMapping.ContentType.FORM_DATA.equals(contentType)) {
            if (value instanceof Map) {
                Map<Object, Object> json = (Map<Object, Object>) value;
                if (json == null || json.size() == 0) {
                    return "无";
                }
                return getPostmanExample(json, "");
            }
        }
        return Theme.parseObjectToJson(value);
    }

    @Override
    public boolean handleMethodHidden(AnnotationHolder annotationHolder) {
        return !annotationHolder.hasAnyOneAnnotatation(AnnotationHolder.QNAME_OF_MAPPING, AnnotationHolder.QNAME_OF_GET_MAPPING, AnnotationHolder.QNAME_OF_POST_MAPPING, AnnotationHolder.QNAME_OF_PUT_MAPPING, AnnotationHolder.QNAME_OF_DELETE_MAPPING);
    }

    @Override
    public void handleJsonExampleAndUrl(Map<String, Object> java2json, boolean singleParam, boolean paramHasRequestBody, boolean methodIsGet, Map<String, String> data, String url) {
        String java2jsonStr = "";
        String paramExample = "";
        if (methodIsGet) {
            // 排除 RequestBody 修饰的参数, 其他参数照常
            if (paramHasRequestBody) {
                if (singleParam) {
                    return;
                }
                removeRequestBodyFromValue(java2json);
            }
            // 暂定示例和 url 都搞
            java2jsonStr = formatJson(java2json, null);
            url = getFullUrl(java2json, url);
            paramExample = java2jsonStr;
        } else {
            // 以 RequestBody 作为示例, 其他参数拼接到 URL 后面
            if (paramHasRequestBody) {
                java2jsonStr = formatJson(getRequestBodyFromValue(java2json), RequestMapping.ContentType.APPLICATION_JSON);
                if (!singleParam) {
                    removeRequestBodyFromValue(java2json);
                    url = getFullUrl(java2json, url);
                    paramExample = formatJson(java2json, null);
                }
            } else {
                java2jsonStr = formatJson(java2json, RequestMapping.ContentType.FORM_DATA);
            }
        }
        data.put("jsonExample", java2jsonStr);
        data.put("paramExample", paramExample);
        data.put("url", url);
    }

    private String getFullUrl(Object java2json, String url) {
        Map<Object, Object> json = (Map<Object, Object>) java2json;
        String url0 = getUrlQuery(json, "");
        return url + url0;
    }

    private String getUrlQuery(Map<Object, Object> json, String prefix) {
        if (json == null || json.size() == 0) {
            return "";
        }
        StringBuilder sbf;
        if ("".equals(prefix)) {
            sbf = new StringBuilder("?");
        } else {
            sbf = new StringBuilder("");
        }
        for (Map.Entry<Object, Object> entry : json.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            handleUrlKeyValue(prefix, sbf, String.valueOf(key), value);
        }
        return sbf.substring(0, sbf.length() - 1);
    }

    private void handleUrlKeyValue(String prefix, StringBuilder sbf, String key, Object value) {
        if (value instanceof Map) {
            Map<Object, Object> map = (Map<Object, Object>) value;
            if (map == null || map.size() == 0) {
                sbf.append(prefix + key).append("=").append(FINAL_DEFAULT_OBJECT_EXAMPLE).append("&");
            } else {
                String example = getUrlQuery(map, prefix + key + ".");
                sbf.append(example).append("&");
            }
        } else if (value instanceof List) {
            List list = (List) value;
            for (int i = 0; i < list.size(); i++) {
                Object obj = list.get(i);
                handleUrlKeyValue(prefix, sbf, key + "[" + i + "]", obj);
            }
        } else if (value instanceof Object[]) {
            Object[] list = (Object[]) value;
            for (int i = 0; i < list.length; i++) {
                Object obj = list[i];
                handleUrlKeyValue(prefix, sbf, key + "[" + i + "]", obj);
            }
        } else {
            String str = String.valueOf(value);
            if (value == null || value.getClass() == Object.class) {
                str = FINAL_DEFAULT_OBJECT_EXAMPLE;
            }
            sbf.append(prefix + key).append("=").append(str).append("&");
        }
    }

    private void removeRequestBodyFromValue(Map<String, Object> java2json) {
        Map json = (Map) java2json;
        Object key = json.remove(MapKeyConstant.HAS_REQUEST_BODY);
        if (key != null && key instanceof String) {
            String key0 = (String) key;
            json.remove(key0);
        }
    }

    private Object getRequestBodyFromValue(Map<String, Object> java2json) {
        Map json = (Map) java2json;
        Object key = json.get(MapKeyConstant.HAS_REQUEST_BODY);
        if (key != null && key instanceof String) {
            String key0 = (String) key;
            return json.get(key0);
        }
        return java2json;
    }

    private String getPostmanExample(Map<Object, Object> json, String prefix) {
        if (json == null || json.size() == 0) {
            return "";
        }
        StringBuilder sbf = new StringBuilder();
        for (Map.Entry<Object, Object> entry : json.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            handleKeyValue(prefix, sbf, String.valueOf(key), value);
        }
        return sbf.substring(0, sbf.length() - 1);
    }

    private void handleKeyValue(String prefix, StringBuilder sbf, String key, Object value) {
        if (value instanceof Map) {
            Map<Object, Object> map = (Map<Object, Object>) value;
            if (map == null || map.size() == 0) {
                sbf.append(prefix + key).append(":").append(FINAL_DEFAULT_OBJECT_EXAMPLE).append("\n");
            } else {
                String example = getPostmanExample(map, prefix + key + ".");
                sbf.append(example).append("\n");
            }
        } else if (value instanceof List) {
            List list = (List) value;
            for (int i = 0; i < list.size(); i++) {
                Object obj = list.get(i);
                handleKeyValue(prefix, sbf, key + "[" + i + "]", obj);
            }
        } else if (value instanceof Object[]) {
            Object[] list = (Object[]) value;
            for (int i = 0; i < list.length; i++) {
                Object obj = list[i];
                handleKeyValue(prefix, sbf, key + "[" + i + "]", obj);
            }
        } else {
            String str = String.valueOf(value);
            if (value == null || value.getClass() == Object.class) {
                str = FINAL_DEFAULT_OBJECT_EXAMPLE;
            }
            sbf.append(prefix + key).append(":").append(str).append("\n");
        }
    }

}
