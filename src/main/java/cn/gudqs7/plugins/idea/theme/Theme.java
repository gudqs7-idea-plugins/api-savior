package cn.gudqs7.plugins.idea.theme;

import cn.gudqs7.plugins.idea.pojo.JsonExampleInfo;
import cn.gudqs7.plugins.idea.pojo.ParamInfo;
import cn.gudqs7.plugins.idea.pojo.ParamLineInfo;
import cn.gudqs7.plugins.idea.pojo.annotation.RequestMapping;
import cn.gudqs7.plugins.idea.savior.BaseSavior;
import cn.gudqs7.plugins.idea.util.AnnotationHolder;
import cn.gudqs7.plugins.idea.util.ParamFilter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiType;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author wq
 */
public interface Theme {
    /**
     * 获取模版
     *
     * @return
     */
    String getMethodPath();

    /**
     * 获取模版
     *
     * @return
     */
    default String getParamContentPath() {
        return getParamContentPath(false);
    }

    /**
     * 获取模版
     *
     * @param returnParam
     * @return
     */
    String getParamContentPath(boolean returnParam);

    /**
     * 获取模版
     *
     * @return
     */
    default String getParamTitlePath() {
        return getParamTitlePath(false);
    }

    /**
     * 获取参数模版-头
     *
     * @param returnParam
     * @return
     */
    String getParamTitlePath(boolean returnParam);

    /**
     * 打印
     *
     * @param goMap
     * @param returnParam
     * @return
     */
    default String printByGoMap(Map<Integer, List<ParamInfo>> goMap, boolean returnParam) {
        StringBuilder all = new StringBuilder();
        for (Map.Entry<Integer, List<ParamInfo>> entry : goMap.entrySet()) {
            Integer key = entry.getKey();

            String[] numberToCnArray = new String[]{"", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
            String cn = key + "";
            if (numberToCnArray.length - 1 >= key) {
                cn = numberToCnArray[key];
            }
            if ("".equals(cn)) {
                continue;
            }
            String levelTitle = "### 第" + cn + "层\n\n";
            all.append(levelTitle);
            List<ParamInfo> paramInfoList = entry.getValue();
            for (ParamInfo paramInfo : paramInfoList) {
                List<ParamLineInfo> paramLineInfos = paramInfo.getAllFields();
                String cn1 = paramInfo.getCn();
                String en = paramInfo.getEn();
                Map<String, String> data = new HashMap<>(16);
                data.put("fieldName", "字段");
                data.put("fieldType", "类型");
                data.put("required", "必填性");
                data.put("fieldDesc", "含义");
                data.put("notes", "其他信息参考");
                data.put("clazzTypeName", en);
                data.put("addition", cn1);

                String allFields = paramLineInfos.stream().map(ParamLineInfo::getLine).collect(Collectors.joining());
                data.put("allFields", allFields);
                String result = BaseSavior.getTemplate(getParamTitlePath(returnParam), data);
                all.append(result);
            }
        }
        String allStr = all.toString();
        if (StringUtils.isBlank(allStr)) {
            return handleNoField(returnParam);
        }
        return allStr;
    }

    default String handleNoField(boolean returnParam) {
        if (returnParam) {
            return "### 出参字段说明\n无任何字段";
        } else {
            return "### 入参字段说明\n无任何字段";
        }
    }

    /**
     * 获取默认的 contentType
     */
    default String getDefaultContentType() {
        return RequestMapping.ContentType.APPLICATION_JSON;
    }

    /**
     * 处理参数列表
     *
     * @param parameter        参数
     * @param annotationHolder 参数的注释/注解信息
     * @param map              大map
     */
    default void handleParameterList(PsiParameter parameter, AnnotationHolder annotationHolder, Map<String, Object> map) {
    }

    /**
     * 格式化示例
     *
     * @param value
     * @param contentType
     * @return
     */
    default String formatJson(Object value, String contentType) {
        return parseObjectToJson(value);
    }

    /**
     * 格式化json
     *
     * @param value 值
     * @return 值
     */
    static String parseObjectToJson(Object value) {
        GsonBuilder gsonBuilder = new GsonBuilder()
                .registerTypeAdapter(JsonExampleInfo.class, new TypeAdapter<JsonExampleInfo>() {
                    @Override
                    public void write(JsonWriter jsonWriter, JsonExampleInfo jsonExampleInfo) throws IOException {
                        if (jsonExampleInfo == null) {
                            jsonWriter.nullValue();
                            return;
                        }
                        Object realVal = jsonExampleInfo.getRealVal();
                        if (realVal instanceof Boolean) {
                            Boolean val = (Boolean) realVal;
                            jsonWriter.value(val);
                        } else if (realVal instanceof Long) {
                            Long val = (Long) realVal;
                            jsonWriter.value(val);
                        } else if (realVal instanceof Double) {
                            Double val = (Double) realVal;
                            jsonWriter.value(val);
                        } else if (realVal instanceof Number) {
                            Number val = (Number) realVal;
                            jsonWriter.value(val);
                        } else {
                            jsonWriter.value(jsonExampleInfo.toString());
                        }

                    }

                    @Override
                    public JsonExampleInfo read(JsonReader jsonReader) throws IOException {
                        return null;
                    }
                })
                .setPrettyPrinting();
        Gson gson = gsonBuilder.create();
        return gson.toJson(value);
    }

    /**
     * 判断参数是否需要跳过
     *
     * @param fieldName
     * @param psiFieldType
     * @param annotationHolder
     * @param oldVal
     * @return
     */
    default Boolean handleHidden(String fieldName, PsiType psiFieldType, AnnotationHolder annotationHolder, Boolean oldVal) {
        if (ParamFilter.isFieldNameNeedJump(fieldName)) {
            return true;
        }
        String typeQname = psiFieldType.getCanonicalText();
        if (ParamFilter.isFieldTypeNeedJump(typeQname)) {
            return true;
        }
        return oldVal;
    }

    /**
     * 处理 url 和 example, 仅 restful 需要用到
     *
     * @param java2json
     * @param singleParam
     * @param paramHasRequestBody
     * @param methodIsGet
     * @param data
     * @param url
     */
    default void handleJsonExampleAndUrl(Map<String, Object> java2json, boolean singleParam, boolean paramHasRequestBody, boolean methodIsGet, Map<String, String> data, String url) {
        if (java2json == null || java2json.size() == 0) {
            data.put("jsonExample", "无");
            return;
        }
        String java2jsonStr = formatJson(java2json, RequestMapping.ContentType.APPLICATION_JSON);
        data.put("jsonExample", java2jsonStr);
    }

    default boolean handleMethodHidden(AnnotationHolder annotationHolder) {
        return false;
    }
}
