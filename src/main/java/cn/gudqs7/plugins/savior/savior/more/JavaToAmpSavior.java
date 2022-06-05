package cn.gudqs7.plugins.savior.savior.more;

import cn.gudqs7.plugins.common.consts.CommonConst;
import cn.gudqs7.plugins.common.enums.MoreCommentTagEnum;
import cn.gudqs7.plugins.common.pojo.resolver.CommentInfo;
import cn.gudqs7.plugins.common.pojo.resolver.StructureAndCommentInfo;
import cn.gudqs7.plugins.common.resolver.comment.AnnotationHolder;
import cn.gudqs7.plugins.common.util.ConfigHolder;
import cn.gudqs7.plugins.common.util.structure.PsiClassUtil;
import cn.gudqs7.plugins.savior.pojo.ComplexInfo;
import cn.gudqs7.plugins.savior.pojo.FieldCommentInfo;
import cn.gudqs7.plugins.savior.reader.Java2ComplexReader;
import cn.gudqs7.plugins.savior.savior.base.AbstractSavior;
import cn.gudqs7.plugins.savior.theme.Theme;
import cn.gudqs7.plugins.savior.util.RestfulUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import lombok.SneakyThrows;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author wq
 * @date 2021/5/19
 */
public class JavaToAmpSavior extends AbstractSavior<Map<String, Object>> {

    private final Java2ComplexReader java2ComplexReader;

    public JavaToAmpSavior(Theme theme) {
        super(theme);
        java2ComplexReader = new Java2ComplexReader(theme);
    }

    @SneakyThrows
    public String generateAmpScheme(PsiClass psiClass, Project project) {
        AnnotationHolder psiClassHolder = AnnotationHolder.getPsiClassHolder(psiClass);
        CommentInfo commentInfo = psiClassHolder.getCommentInfo();
        boolean hidden = commentInfo.isHidden(false);
        if (hidden) {
            return null;
        }

        // 根据 @Order 注解 以及字母顺序, 从小到大排序
        PsiMethod[] methods = PsiClassUtil.getAllMethods(psiClass);
        Arrays.sort(methods, this::orderByMethod);
        String interfaceClassName = psiClass.getQualifiedName();
        Map<String, Object> apis = new HashMap<>(8);

        for (PsiMethod method : methods) {
            if (this.filterMethod(method)) {
                continue;
            }
            String actionName = getMethodActionName(method);
            if (StringUtils.isBlank(actionName)) {
                continue;
            }

            Map<String, Object> ampApi = generateAmpApi(project, interfaceClassName, method);
            if (ampApi != null) {
                apis.put(actionName, ampApi);
            }
        }

        if (apis.size() == 0) {
            return null;
        }

        Map<String, Object> root = new HashMap<>(4);
        root.put("apis", apis);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(root);
        JsonNode jsonNodeTree = objectMapper.readTree(json);
        return new YAMLMapper().writeValueAsString(jsonNodeTree);
    }

    public Map<String, Object> generateAmpApi(Project project, String interfaceClassName, PsiMethod publicMethod) {
        return getDataByMethod(project, interfaceClassName, publicMethod, false);
    }

    @Override
    protected Map<String, Object> getDataByStructureAndCommentInfo(Project project, PsiMethod publicMethod, CommentInfo commentInfo, String interfaceClassName, StructureAndCommentInfo paramStructureAndCommentInfo, StructureAndCommentInfo returnStructureAndCommentInfo, Map<String, Object> param) {
        Map<String, String> config = ConfigHolder.getConfig();
        if (config == null) {
            return null;
        }

        String url = commentInfo.getUrl("");
        String method = commentInfo.getMethod("");
        String methodName = publicMethod.getName();
        String interfaceName = commentInfo.getValue(methodName);
        String rwType = commentInfo.getSingleStr(MoreCommentTagEnum.AMP_RW.getTag(), "read");

        String noHostUrl = url.replace("http://", "");
        if (noHostUrl.contains("/")) {
            noHostUrl = noHostUrl.substring(noHostUrl.indexOf("/"));
        }

        Map<String, Object> java2jsonMap = java2ComplexReader.read(paramStructureAndCommentInfo);
        Map<String, Object> returnJava2jsonMap = java2ComplexReader.read(returnStructureAndCommentInfo);

        Map<String, Object> data = new LinkedHashMap<>(16);
        // 概述
        data.put("summary", interfaceName);
        // HTTP 方法, 单选
        data.put("methods", new String[]{RestfulUtil.getFirstMethod(method).toLowerCase()});
        // 协议类型
        data.put("schemes", new String[]{"http", "https"});
        // 鉴权方法
        data.put("security", Collections.singleton(getAk()));
        // 读写类型 read/write/readAndWrite
        data.put("operationType", rwType);
        // 可见性
        data.put("visibility", "Public");
        // 是否弃用
        data.put("deprecated", false);
        // 网关配置
        data.put("gatewayOptions", getGatewayOptions(config));
        // 后端配置
        data.put("backendService", getBackendService(config, noHostUrl));
        // 策略配置
        data.put("policies", getPolicies(config));
        // 参数配置
        data.put("parameters", getParameters(config, java2jsonMap));
        // 返回值配置
        data.put("responses", getResponses(config, returnJava2jsonMap));
        // 错误信息映射
        data.put("errorMapping", getErrorMapping(config));
        data.put("variables", getVariables(config, noHostUrl));
        return data;
    }

    private Map<String, Object> getVariables(Map<String, String> config, String url) {
        Map<String, Object> pre = new HashMap<>(8);

        String dailyHost = config.get("amp.host.daily");
        String preHost = config.get("amp.host.pre");

        pre.put("$.backendService.url", preHost + url);
        pre.put("$.backendService.httpsValidation", "__null__");
        pre.put("$.backendService.signKeyName", "__null__");
        pre.put("$.backendService.sign", "__null__");
        pre.put("$.backendService.serviceRouteName", "");
        pre.put("$.backendService.version", "__null__");
        pre.put("$.backendService.signPolicy", "__null__");
        pre.put("$.backendService.retries", -1);

        Map<String, Object> daily = new HashMap<>(8);
        daily.put("$.policies.controlPolicyName", "__null__");
        daily.put("$.backendService.url", dailyHost + url);
        daily.put("$.policies.grayScalePolicyName", "__null__");
        daily.put("$.policies.rateLimitPolicy.ipRateLimit", "__null__");
        daily.put("$.backendService.signKeyName", "__null__");
        daily.put("$.policies.rateLimitPolicy.userRateLimit", "__null__");
        daily.put("$.backendService.version", "__null__");
        daily.put("$.policies.rateLimitPolicy.apiRateLimit", 10000L);
        daily.put("$.backendService.httpsValidation", "__null__");
        daily.put("$.policies.rateLimitPolicy.specialRateLimitPolicyName", "__null__");
        daily.put("$.backendService.sign", "__null__");
        daily.put("$.backendService.serviceRouteName", "");
        daily.put("$.policies.rateLimitPolicy.unit", "Second");
        daily.put("$.backendService.signPolicy", "__null__");
        daily.put("$.backendService.retries", -1);
        daily.put("$.schemes", new String[]{"HTTP", "HTTPS"});

        Map<String, Object> variables = new HashMap<>(8);
        variables.put("pre", pre);
        variables.put("daily", daily);
        return variables;
    }

    private Map<String, Object> getErrorMapping(Map<String, String> config) {
        Map<String, Object> errorMapping = new HashMap<>(8);
        // 错误条件判断
        errorMapping.put("errorExpression", "code!=200");
        errorMapping.put("codeField", "code");
        errorMapping.put("errorMessageField", "msg");
        errorMapping.put("httpStatusCodeField", "httpStatusCode");
        return errorMapping;
    }

    private Map<String, Object> getResponses(Map<String, String> config, Map<String, Object> resultJava2json) {
        String dataMode = config.getOrDefault("amp.data.mode", "list");
        Map<String, Object> properties = getPropertiesByFieldMap(resultJava2json, dataMode, true);
        Map<String, Object> schema = new LinkedHashMap<>(2);
        schema.put("title", "Schema of Response");
        schema.put("type", "object");
        schema.put("properties", properties);
        Map<String, Object> resp200 = new LinkedHashMap<>(2);
        resp200.put("schema", schema);
        Map<String, Object> response = new LinkedHashMap<>(2);
        response.put("200", resp200);
        return response;
    }

    private List<Map<String, Object>> getParameters(Map<String, String> config, Map<String, Object> java2json) {
        List<Map<String, Object>> parameterList = new ArrayList<>();
        Set<String> existsParam = new HashSet<>(8);
        addParameterByConfig(existsParam, parameterList, config, "amp.param.system", "system");
        addParameterByConfig(existsParam, parameterList, config, "amp.param.req", "formData");

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
                    fieldDesc = fieldDesc.replaceAll(CommonConst.BREAK_LINE, "\n");
                }

                String upperKey = key.substring(0, 1).toUpperCase() + key.substring(1);
                if (commentInfo != null) {
                    upperKey = commentInfo.getSingleStr(MoreCommentTagEnum.AMP_FIELD.getTag(), upperKey);
                }

                String afterIgnoreCase = upperKey.toLowerCase();
                if (existsParam.contains(afterIgnoreCase)) {
                    continue;
                }
                existsParam.add(afterIgnoreCase);

                String type;
                String format = null;
                if (realVal instanceof Integer) {
                    type = "integer";
                    format = "int32";
                } else if (realVal instanceof Long) {
                    type = "integer";
                    format = "int64";
                } else if (realVal instanceof Float) {
                    type = "number";
                    format = "float";
                } else if (realVal instanceof Double) {
                    type = "number";
                    format = "double";
                } else if (realVal instanceof Boolean) {
                    type = "boolean";
                } else if (realVal instanceof String) {
                    type = "string";
                } else {
                    continue;
                }
                parameterList.add(getParameter(upperKey, type, key, "formData", format, fieldDesc));
            }

        }
        return parameterList;
    }

    private Map<String, Object> getPolicies(Map<String, String> config) {
        Map<String, Object> rateLimitPolicy = new LinkedHashMap<>(2);
        // API频率
        rateLimitPolicy.put("apiRateLimit", 100);
        // 流控单位
        rateLimitPolicy.put("unit", "Second");
        // 单用户调用频率
        rateLimitPolicy.put("userRateLimit", 100);
        Map<String, Object> policies = new HashMap<>(2);
        policies.put("rateLimitPolicy", rateLimitPolicy);
        return policies;
    }

    private Map<String, Object> getBackendService(Map<String, String> config, String url) {
        String preHost = config.get("amp.host.pro");
        String appName = config.getOrDefault("amp.backendService.appName", "xxx");
        Map<String, Object> backendService = new LinkedHashMap<>(8);
        backendService.put("protocol", "http");
        backendService.put("appName", appName);
        backendService.put("retries", -1);
        backendService.put("timeout", 10000L);
        backendService.put("url", preHost + url);
        backendService.put("signKeyName", "aiops");
        backendService.put("sign", true);
        backendService.put("signPolicy", "Local");
        return backendService;
    }

    private Map<String, Object> getGatewayOptions(Map<String, String> config) {
        Map<String, Object> gatewayOptions = new LinkedHashMap<>(8);
        // 记录日志
        gatewayOptions.put("responseLog", true);
        // 获取资源所有者
        gatewayOptions.put("keepClientResourceOwnerId", true);
        // AK Proven
        gatewayOptions.put("akProvenStatus", "Disable");
        // 支持文件中转传输
        gatewayOptions.put("fileTransfer", false);
        gatewayOptions.put("showJsonItemName", false);
        return gatewayOptions;
    }

    private Map<String, Object> getAk() {
        Map<String, Object> ak = new HashMap<>(2);
        ak.put("AK", new ArrayList<>());
        return ak;
    }

    private Map<String, Object> getPropertiesByFieldMap(Map<String, Object> fieldInfoMap, String dataMode, boolean needUpper) {
        Map<String, Object> properties = new LinkedHashMap<>(2);
        for (Map.Entry<String, Object> entry : fieldInfoMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String upperKey = key;
            if (needUpper) {
                upperKey = key.substring(0, 1).toUpperCase() + key.substring(1);
            }


            if (value instanceof ComplexInfo) {
                ComplexInfo complexInfo = (ComplexInfo) value;
                FieldCommentInfo fieldCommentInfo = complexInfo.getFieldCommentInfo();
                CommentInfo commentInfo = complexInfo.getCommentInfo();
                String fieldDesc = fieldCommentInfo.getFieldDesc();
                if (StringUtils.isNotBlank(fieldDesc)) {
                    fieldDesc = fieldDesc.replaceAll(CommonConst.BREAK_LINE, "\n");
                }
                if (commentInfo != null) {
                    upperKey = commentInfo.getSingleStr(MoreCommentTagEnum.AMP_FIELD.getTag(), upperKey);
                }

                Object realVal = complexInfo.getRealVal();

                String type = null;
                String format = null;
                if (realVal instanceof Integer) {
                    type = "integer";
                    format = "int32";
                } else if (realVal instanceof Long) {
                    type = "integer";
                    format = "int64";
                } else if (realVal instanceof Float) {
                    type = "number";
                    format = "float";
                } else if (realVal instanceof Double) {
                    type = "number";
                    format = "double";
                } else if (realVal instanceof Boolean) {
                    type = "boolean";
                } else if (realVal instanceof String) {
                    type = "string";
                }
                if (type != null) {
                    properties.put(upperKey, getResponse(type, key, format, fieldDesc));
                }
            } else if (value instanceof List) {
                Map<String, Object> response;
                List list = (List) value;
                if ("list".equals(dataMode)) {
                    response = getResponseByArray(key, list);
                    properties.put(upperKey, response);
                } else if ("map".equals(dataMode)) {
                    properties.put(upperKey, getResponse("object", key, null, ""));
                }
            } else if (value instanceof Map) {
                Map valueMap = (Map) value;
                Map<String, Object> response;
                if ("list".equals(dataMode)) {
                    Map<String, Object> items = new LinkedHashMap<>(8);
                    items.put("backendName", key);
                    items.put("type", "object");
                    items.put("properties", getPropertiesByFieldMap(valueMap, dataMode, false));
                    properties.put(upperKey, items);
                } else if ("map".equals(dataMode)) {
                    properties.put(upperKey, getResponse("object", key, null, ""));
                }
            }
        }
        return properties;
    }

    private Map<String, Object> getResponseByArray(String backendName, List realValList) {
        Map<String, Object> properties = new LinkedHashMap<>(8);
        if (CollectionUtils.isNotEmpty(realValList)) {
            Object realVal = realValList.get(0);
            if (realVal instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) realVal;
                properties = getPropertiesByFieldMap(map, "list", false);
            }
        }

        Map<String, Object> items = new LinkedHashMap<>(8);
        items.put("type", "object");
        items.put("properties", properties);

        Map<String, Object> response = new LinkedHashMap<>(8);
        response.put("type", "array");
        response.put("items", items);
        response.put("backendName", backendName);
        response.put("itemName", "List");
        return response;
    }

    private Map<String, Object> getResponse(String type, String backendName, String format, String title) {
        Map<String, Object> response = new LinkedHashMap<>();
        if (StringUtils.isNotBlank(title)) {
            response.put("title", title);
        }
        response.put("type", type);
        if (StringUtils.isNotBlank(format)) {
            response.put("format", format);
        }
        response.put("backendName", backendName);
        return response;
    }

    private void addParameterByConfig(Set<String> existsParam, List<Map<String, Object>> parameterList, Map<String, String> config, String configKey, String in) {
        String systemParamKey = config.get(configKey);
        if (StringUtils.isNotBlank(systemParamKey)) {
            String[] systemKeyArray = systemParamKey.split(",");
            Set<String> systemKeySet = new HashSet<>(Arrays.asList(systemKeyArray));
            for (String systemKey : systemKeySet) {
                String[] array = systemKey.split("-");
                if (array.length > 2) {
                    String key = array[0];
                    String type = array[1];
                    String backKey = array[2];

                    String afterIgnoreCase = key.toLowerCase();
                    if (existsParam.contains(afterIgnoreCase)) {
                        continue;
                    }
                    existsParam.add(afterIgnoreCase);

                    String format = null;
                    if (array.length > 3) {
                        format = array[3];
                    }
                    parameterList.add(getParameter(key, type, backKey, in, format, ""));
                }
            }
        }
    }

    private Map<String, Object> getParameter(String key, String type, String backKey, String in, String format, String title) {
        Map<String, Object> schema = new LinkedHashMap<>(32);
        schema.put("type", type);
        if (StringUtils.isNotBlank(format)) {
            schema.put("format", format);
        }
        schema.put("backendName", backKey);
        if (StringUtils.isNotBlank(title)) {
            schema.put("title", title);
        }
        Map<String, Object> parameter = new LinkedHashMap<>(32);
        parameter.put("name", key);
        parameter.put("in", in);
        parameter.put("schema", schema);
        return parameter;
    }

}
