package cn.gudqs7.plugins.savior.docer.savior.more;

import cn.gudqs7.plugins.savior.docer.annotation.AnnotationHolder;
import cn.gudqs7.plugins.savior.docer.constant.CommentConst;
import cn.gudqs7.plugins.savior.docer.enums.MoreCommentTagEnum;
import cn.gudqs7.plugins.savior.docer.pojo.ComplexInfo;
import cn.gudqs7.plugins.savior.docer.pojo.FieldCommentInfo;
import cn.gudqs7.plugins.savior.docer.pojo.StructureAndCommentInfo;
import cn.gudqs7.plugins.savior.docer.pojo.annotation.CommentInfo;
import cn.gudqs7.plugins.savior.docer.reader.Java2ComplexReader;
import cn.gudqs7.plugins.savior.docer.savior.base.AbstractSavior;
import cn.gudqs7.plugins.savior.docer.theme.Theme;
import cn.gudqs7.plugins.savior.docer.util.ActionUtil;
import cn.gudqs7.plugins.savior.docer.util.ConfigHolder;
import cn.gudqs7.plugins.savior.docer.util.HttpUtil;
import cn.gudqs7.plugins.savior.docer.util.JsonUtil;
import cn.gudqs7.plugins.savior.util.PsiClassUtil;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

/**
 * @author wq
 * @date 2021/5/19
 */
public class JavaToOneApiSavior extends AbstractSavior<Void> {

    private final Java2ComplexReader java2ComplexReader;

    public JavaToOneApiSavior(Theme theme) {
        super(theme);
        this.java2ComplexReader = new Java2ComplexReader(theme);
    }

    public void generateOneApi(PsiClass psiClass, Project project) {
        AnnotationHolder psiClassHolder = AnnotationHolder.getPsiClassHolder(psiClass);
        CommentInfo commentInfo = psiClassHolder.getCommentInfo();
        boolean hidden = commentInfo.isHidden(false);
        if (hidden) {
            return;
        }

        // 根据 @Order 注解 以及字母顺序, 从小到大排序
        PsiMethod[] methods = PsiClassUtil.getAllMethods(psiClass);
        Arrays.sort(methods, this::orderByMethod);
        String interfaceClassName = psiClass.getQualifiedName();

        for (PsiMethod method : methods) {
            if (this.filterMethod(method)) {
                continue;
            }
            String actionName = getMethodActionName(method);
            if (StringUtils.isBlank(actionName)) {
                continue;
            }

            ApplicationManager.getApplication().invokeAndWait(()->{
                generateAmpApi(project, interfaceClassName, method);
            });

        }
    }

    public void generateAmpApi(Project project, String interfaceClassName, PsiMethod publicMethod) {
        getDataByMethod(project, interfaceClassName, publicMethod, false);
    }

    @Override
    protected Void getDataByStructureAndCommentInfo(Project project, PsiMethod publicMethod, CommentInfo commentInfo, String interfaceClassName, StructureAndCommentInfo paramStructureAndCommentInfo, StructureAndCommentInfo returnStructureAndCommentInfo, Map<String, Object> param) {
        Map<String, String> config = ConfigHolder.getConfig();
        if (config == null) {
            return null;
        }

        String methodName = publicMethod.getName();
        String interfaceName = commentInfo.getValue(methodName);
        String actionName = commentInfo.getSingleStr(MoreCommentTagEnum.AMP_ACTION_NAME.getTag(), null);
        String dataSize = commentInfo.getSingleStr(MoreCommentTagEnum.AMP_DATA_SIZE.getTag(), "2");
        String projectCode = config.get("oneApi.projectCode");
        String catalogId = config.get("oneApi.catalogId");
        String cookie = config.get("oneApi.cookie");
        String createUrl = config.get("oneApi.createUrl");
        String updateTagUrl = config.get("oneApi.updateTagUrl");
        String noTag = config.getOrDefault("oneApi.noTag", "false");
        String noMain = config.getOrDefault("oneApi.noMain", "false");

        if (StringUtils.isBlank(actionName)
                || StringUtils.isBlank(projectCode)
                || StringUtils.isBlank(catalogId)
                || StringUtils.isBlank(cookie)
        ) {
            return null;
        }

        String defaultTagName = "default";
        String apiName = actionName + ".json";

        Map<String, String> header = new LinkedHashMap<>(4);
        header.put("content-type", "application/json");
        header.put("accept", "application/json, text/json");
        header.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.84 Safari/537.36");
        header.put("cookie", cookie);

        boolean noMain0 = "true".equals(noMain);
        boolean noTag0 = "true".equals(noTag);

        if (noMain0 && noTag0) {
            return null;
        }

        Map<String, Object> returnJava2jsonMap = java2ComplexReader.read(returnStructureAndCommentInfo);
        if (!noMain0) {
            Map<String, Object> java2jsonMap = java2ComplexReader.read(paramStructureAndCommentInfo);
            saveOrUpdateMain(java2jsonMap, returnJava2jsonMap, config, interfaceName, actionName, projectCode, catalogId, createUrl, defaultTagName, apiName, header);
        }
        if (!noTag0) {
            updateTag(returnJava2jsonMap, config, actionName, projectCode, updateTagUrl, defaultTagName, apiName, header, dataSize);
        }
        return null;
    }

    private void updateTag(Map<String, Object> resultJava2json, Map<String, String> config, String actionName, String projectCode, String updateTagUrl, String defaultTagName, String apiName, Map<String, String> header, String dataSize) {
        Map<String, Object> data = getResultExample(config, resultJava2json, dataSize, true);
        Map<String, Object> tagResponse = new LinkedHashMap<>(8);
        tagResponse.put("successResponse", true);
        tagResponse.put("code", "200");
        tagResponse.put("data", data);
        tagResponse.put("requestId", "@guid");

        Map<String, Object> tagBody = new LinkedHashMap<>(8);
        tagBody.put("_csrf", "");
        tagBody.put("tagName", defaultTagName);
        tagBody.put("apiName", apiName);
        tagBody.put("projectCode", projectCode);
        tagBody.put("tagResponse", tagResponse);

        TypeAdapter<ComplexInfo> typeAdapter = new TypeAdapter<>() {
            @Override
            public void write(JsonWriter jsonWriter, ComplexInfo complexInfo) throws IOException {
                if (complexInfo == null) {
                    jsonWriter.nullValue();
                    return;
                }
                CommentInfo commentInfo = complexInfo.getCommentInfo();
                if (commentInfo != null) {
                    String mockjs = commentInfo.getSingleStr(MoreCommentTagEnum.AMP_MOCK_VAL.getTag(), "");
                    if (StringUtils.isNotBlank(mockjs)) {
                        jsonWriter.value(mockjs);
                        return;
                    }
                }
                Object realVal = complexInfo.getRealVal();
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
                    jsonWriter.value(complexInfo.toString());
                }
            }

            @Override
            public ComplexInfo read(JsonReader jsonReader) throws IOException {
                return null;
            }
        };
        String tagBodyStr = JsonUtil.toJson(tagBody, typeAdapter);
        String tagRes = HttpUtil.sendHttpWithBody(updateTagUrl, "POST", tagBodyStr, header);
        showErrorTip(actionName, tagRes, "update tag");
    }

    private void saveOrUpdateMain(Map<String, Object> java2json, Map<String, Object> resultJava2json, Map<String, String> config, String interfaceName, String actionName, String projectCode, String catalogId, String createUrl, String defaultTagName, String apiName, Map<String, String> header) {
        List<Map<String, Object>> requestParamList = getRequestParamList(config, java2json);
        List<Map<String, Object>> responseParamList = getResponseParamList(config, resultJava2json);
        Map<String, Object> creator = new LinkedHashMap<>(32);
        creator.put("workNo", "SYSTEM");
        creator.put("name", "SYSTEM");
        creator.put("nickName", "SYSTEM");

        Map<String, Object> requestBody = new LinkedHashMap<>(32);
        requestBody.put("_csrf", "");
        requestBody.put("pid", Integer.parseInt(catalogId));
        requestBody.put("catalogId", Integer.parseInt(catalogId));
        requestBody.put("projectCode", projectCode);
        requestBody.put("apiName", apiName);
        requestBody.put("method", "ALL");
        requestBody.put("description", interfaceName);
        requestBody.put("currentTag", defaultTagName);
        requestBody.put("requestParams", requestParamList);
        requestBody.put("responseParams", responseParamList);
        requestBody.put("headerParams", new ArrayList<>());
        requestBody.put("delay", 0);
        requestBody.put("mockjs", true);
        requestBody.put("creator", creator);
        requestBody.put("modifier", creator);
        requestBody.put("gmtCreate", "2022-03-29T12:19:37.000Z");
        requestBody.put("gmtModified", "2022-03-29T12:19:37.000Z");

        String createBodyStr = JsonUtil.toJson(requestBody);
        String createRes = HttpUtil.sendHttpWithBody(createUrl, "POST", createBodyStr, header);
        showErrorTip(actionName, createRes, "create");
    }

    private void showErrorTip(String actionName, String createRes, final String operate) {
        if (!createRes.startsWith("{")) {
            String content = actionName + " " + operate + " error, cause by " + createRes;
            ActionUtil.showNotification(content, NotificationDisplayType.BALLOON, NotificationType.ERROR);
            return;
        }
        Map map = JsonUtil.fromJson(createRes, Map.class);
        if (map != null) {
            Object success = map.get("success");
            if (success instanceof Boolean) {
                Boolean success0 = (Boolean) success;
                if (success0) {
                    String content = actionName + " " + operate + " success!";
                    ActionUtil.showNotification(content, NotificationDisplayType.BALLOON, NotificationType.INFORMATION);
                } else {
                    String content = actionName + " " + operate + " error, cause by " + createRes;
                    ActionUtil.showNotification(content, NotificationDisplayType.BALLOON, NotificationType.ERROR);
                }
            }
        }
    }

    @NotNull
    private Map<String, Object> getResultExample(Map<String, String> config, Map<String, Object> resultJava2json, String dataSize, boolean needUpper) {
        Map<String, Object> map = new LinkedHashMap<>(8);

        for (Map.Entry<String, Object> entry : resultJava2json.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            getMapByVal(config, dataSize, map, key, value, needUpper);
        }

        return map;
    }

    private void getMapByVal(Map<String, String> config, String dataSize, Map<String, Object> map, String key, Object value, boolean needUpper) {
        String upperKey = key;
        if (needUpper) {
            upperKey = key.substring(0, 1).toUpperCase() + key.substring(1);
        }

        if (value instanceof ComplexInfo) {
            ComplexInfo complexInfo = (ComplexInfo) value;
            FieldCommentInfo fieldCommentInfo = complexInfo.getFieldCommentInfo();
            CommentInfo commentInfo = complexInfo.getCommentInfo();
            if (commentInfo != null) {
                upperKey = commentInfo.getSingleStr(MoreCommentTagEnum.AMP_FIELD.getTag(), upperKey);
                upperKey = commentInfo.getSingleStr(MoreCommentTagEnum.AMP_MOCK_KEY.getTag(), upperKey);
            }
            map.put(upperKey, value);
        } else if (value instanceof Map) {
            Map<String, Object> mapValue = (Map<String, Object>) value;
            map.put(upperKey, getResultExample(config, mapValue, dataSize, needUpper));
        } else if (value instanceof List) {
            List list = (List) value;
            if (list.size() == 1) {
                Object val = list.get(0);
                if (StringUtils.isNotBlank(dataSize)) {
                    list.clear();
                    int dataSize0 = Integer.parseInt(dataSize);
                    for (int i = 0; i < dataSize0; i++) {
                        Map<String, Object> map0 = new LinkedHashMap<>(8);
                        getMapByVal(config, dataSize, map0, key, val, false);
                        if (map0.keySet().size()>0) {
                            String afterKey = new ArrayList<>(map0.keySet()).get(0);
                            Object afterVal = map0.get(afterKey);
                            if (afterVal != null) {
                                list.add(afterVal);
                            }
                        }
                    }
                    if (list.size() == 0) {
                        list.add(val);
                    }
                }
            }
            map.put(upperKey, list);
        }
    }

    @NotNull
    private List<Map<String, Object>> getResponseParamList(Map<String, String> config, Map<String, Object> resultJava2json) {
        List<Map<String, Object>> responseList = new ArrayList<>();
        Map<String, Object> properties = new LinkedHashMap<>(2);
        for (Map.Entry<String, Object> entry : resultJava2json.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String upperKey = key.substring(0, 1).toUpperCase() + key.substring(1);

            if (value instanceof ComplexInfo) {
                ComplexInfo complexInfo = (ComplexInfo) value;
                FieldCommentInfo fieldCommentInfo = complexInfo.getFieldCommentInfo();
                CommentInfo commentInfo = complexInfo.getCommentInfo();
                String example = fieldCommentInfo.getExample();
                String fieldDesc = fieldCommentInfo.getFieldDesc();
                if (StringUtils.isNotBlank(fieldDesc)) {
                    fieldDesc = fieldDesc.replaceAll(CommentConst.BREAK_LINE, "\n");
                }
                if (commentInfo != null) {
                    upperKey = commentInfo.getSingleStr(MoreCommentTagEnum.AMP_FIELD.getTag(), upperKey);
                }

                Object realVal = complexInfo.getRealVal();

                String type = null;
                if (realVal instanceof Integer) {
                    type = "Number";
                } else if (realVal instanceof Long) {
                    type = "Number";
                } else if (realVal instanceof Float) {
                    type = "Number";
                } else if (realVal instanceof Double) {
                    type = "Number";
                } else if (realVal instanceof Boolean) {
                    type = "Boolean";
                } else if (realVal instanceof String) {
                    type = "String";
                }
                if (type != null) {
                    responseList.add(getResponse(upperKey, type, fieldDesc));
                }
            } else if (value instanceof List) {
                responseList.add(getResponse(upperKey, "List", "详见Mock数据以及语雀文档"));
            }
        }
        return responseList;
    }

    @NotNull
    private List<Map<String, Object>> getRequestParamList(Map<String, String> config, Map<String, Object> java2json) {
        Set<String> existsParam = initExistsParam();
        List<Map<String, Object>> requestParamList = new ArrayList<>();
        addParameterByConfig(existsParam, requestParamList, config, "oneApi.param.req");

        for (Map.Entry<String, Object> entry : java2json.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof ComplexInfo) {
                ComplexInfo complexInfo = (ComplexInfo) value;
                CommentInfo commentInfo = complexInfo.getCommentInfo();
                Object realVal = complexInfo.getRealVal();
                FieldCommentInfo fieldCommentInfo = complexInfo.getFieldCommentInfo();
                String example = fieldCommentInfo.getExample();
                String fieldDesc = fieldCommentInfo.getFieldDesc();
                if (StringUtils.isNotBlank(fieldDesc)) {
                    fieldDesc = fieldDesc.replaceAll(CommentConst.BREAK_LINE, "\n");
                }

                String require = "false";
                String upperKey = key.substring(0, 1).toUpperCase() + key.substring(1);
                if (commentInfo != null) {
                    upperKey = commentInfo.getSingleStr(MoreCommentTagEnum.AMP_FIELD.getTag(), upperKey);
                    require = String.valueOf(commentInfo.isRequired(false));
                }

                String afterIgnoreCase = upperKey.toLowerCase();
                if (existsParam.contains(afterIgnoreCase)) {
                    continue;
                }
                existsParam.add(afterIgnoreCase);

                String type;

                if (realVal instanceof Integer) {
                    type = "Number";
                } else if (realVal instanceof Long) {
                    type = "Number";
                } else if (realVal instanceof Float) {
                    type = "Number";
                } else if (realVal instanceof Double) {
                    type = "Number";
                } else if (realVal instanceof Boolean) {
                    type = "Boolean";
                } else if (realVal instanceof String) {
                    type = "String";
                } else {
                    continue;
                }
                requestParamList.add(getParameter(upperKey, type, require, fieldDesc));
            }

        }
        return requestParamList;
    }

    @NotNull
    private Set<String> initExistsParam() {
        Set<String> param = new HashSet<>(8);
        param.add("callertype");
        param.add("calleruid");
        return param;
    }

    private void addParameterByConfig(Set<String> existsParam, List<Map<String, Object>> parameterList, Map<String, String> config, String configKey) {
        String systemParamKey = config.get(configKey);
        Set<String> systemKeySet = new HashSet<>();
        if (StringUtils.isNotBlank(systemParamKey)) {
            String[] systemKeyArray = systemParamKey.split(",");
            systemKeySet.addAll(Arrays.asList(systemKeyArray));
        }

        for (String systemKey : systemKeySet) {
            String[] array = systemKey.split("-");
            if (array.length > 2) {
                String key = array[0];
                String type = array[1];
                String require = array[2];

                String afterIgnoreCase = key.toLowerCase();
                if (existsParam.contains(afterIgnoreCase)) {
                    continue;
                }
                existsParam.add(afterIgnoreCase);

                String title = null;
                if (array.length > 3) {
                    title = array[3];
                }
                parameterList.add(getParameter(key, type, require, title));
            }
        }
    }

    private Map<String, Object> getResponse(String key, String type, String title) {
        Map<String, Object> response = new LinkedHashMap<>(8);
        response.put("name", key);
        response.put("type", type);
        response.put("description", title);
        return response;
    }

    @NotNull
    private Map<String, Object> getParameter(String key, String type, String require, String title) {
        Map<String, Object> parameter = new LinkedHashMap<>(32);
        parameter.put("require", Boolean.parseBoolean(require));
        parameter.put("name", key);
        parameter.put("type", type);
        parameter.put("description", title);
        return parameter;
    }

}
