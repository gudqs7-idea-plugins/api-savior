package cn.gudqs7.plugins.docer.theme;

import cn.gudqs7.plugins.docer.annotation.AnnotationHolder;
import cn.gudqs7.plugins.docer.constant.FieldType;
import cn.gudqs7.plugins.docer.constant.MapKeyConstant;
import cn.gudqs7.plugins.docer.constant.ThemeType;
import cn.gudqs7.plugins.docer.pojo.ParamInfo;
import cn.gudqs7.plugins.docer.pojo.ParamLineInfo;
import cn.gudqs7.plugins.docer.pojo.PostmanKvInfo;
import cn.gudqs7.plugins.docer.pojo.StructureAndCommentInfo;
import cn.gudqs7.plugins.docer.pojo.annotation.CommentInfo;
import cn.gudqs7.plugins.docer.pojo.annotation.RequestMapping;
import cn.gudqs7.plugins.docer.reader.Java2BulkReader;
import cn.gudqs7.plugins.docer.savior.base.BaseSavior;
import cn.gudqs7.plugins.docer.util.JsonUtil;
import cn.gudqs7.plugins.docer.util.RestfulUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wq
 */
public class RestfulTheme implements Theme {

    public static final String FINAL_DEFAULT_OBJECT_EXAMPLE = "{}";
    private static RestfulTheme instance;

    private final Java2BulkReader java2BulkReader;

    public RestfulTheme() {
        java2BulkReader = new Java2BulkReader(this);
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
    public ThemeType getThemeType() {
        return ThemeType.RESTFUL;
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
        return "";
    }

    @Override
    public String getDefaultContentType() {
        return RequestMapping.ContentType.X_WWW_FORM_URLENCODED;
    }

    @Override
    public boolean handleMethodHidden(AnnotationHolder annotationHolder) {
        // 过滤非 Controller 的方法
        return !annotationHolder.hasAnyOneAnnotation(AnnotationHolder.QNAME_OF_MAPPING, AnnotationHolder.QNAME_OF_GET_MAPPING, AnnotationHolder.QNAME_OF_POST_MAPPING, AnnotationHolder.QNAME_OF_PUT_MAPPING, AnnotationHolder.QNAME_OF_DELETE_MAPPING);
    }

    @Override
    public void afterCollectData(Map<String, Object> dataByStr, Project project, PsiMethod publicMethod, String interfaceClassName, CommentInfo commentInfo, StructureAndCommentInfo paramStructureAndCommentInfo, StructureAndCommentInfo returnStructureAndCommentInfo, Map<String, Object> java2jsonMap, Map<String, Object> returnJava2jsonMap, String java2jsonStr, String returnJava2jsonStr) {
        if (java2jsonMap == null || java2jsonMap.size() == 0) {
            dataByStr.put("jsonExample", "此接口无任何入参");
            return;
        }
        // 1.获取 json示例 或 bulk 示例
        // 2.补全 URL query 部分
        String url = commentInfo.getUrl("");
        String contentType = commentInfo.getContentType(getDefaultContentType());
        String method = commentInfo.getMethod("");
        String method0 = RestfulUtil.getFirstMethod(method);
        boolean firstMethodIsGet = RequestMapping.Method.GET.equals(method0);

        HashMap<String, Object> data = new HashMap<>(2);
        data.put("removeRequestBody", true);
        List<PostmanKvInfo> queryList = java2BulkReader.read(paramStructureAndCommentInfo, data);
        String query = RestfulUtil.getUrlQuery(queryList, false);

        if (firstMethodIsGet) {
            // GET
            url = url + query;
            dataByStr.put("jsonExample", RestfulUtil.getPostmanBulkByKvList(queryList));
        } else {
            switch (contentType) {
                case RequestMapping.ContentType.APPLICATION_JSON:
                    // POST + requestBody
                    url = url + query;
                    Object key = java2jsonMap.get(MapKeyConstant.HAS_REQUEST_BODY);
                    if (key instanceof String) {
                        String key0 = (String) key;
                        dataByStr.put("jsonExample", JsonUtil.toJson(java2jsonMap.get(key0)));
                    }
                    break;
                case RequestMapping.ContentType.FORM_DATA:
                case RequestMapping.ContentType.X_WWW_FORM_URLENCODED:
                    // POST + form-data || POST + x-www/form-data
                    List<PostmanKvInfo> kvList = java2BulkReader.read(paramStructureAndCommentInfo);
                    dataByStr.put("jsonExample", RestfulUtil.getPostmanBulkByKvList(kvList));
                    break;
                default:
                    break;
            }
        }
        dataByStr.put("url", url);
    }

    @Override
    public boolean handleParameter(StructureAndCommentInfo structureAndCommentInfo, Map<String, Object> map, String fieldName) {
        if (structureAndCommentInfo.getCommentInfo() != null) {
            AnnotationHolder parent = structureAndCommentInfo.getCommentInfo().getParent();
            boolean hasAnnotation = parent.hasAnnotation(AnnotationHolder.QNAME_OF_REQUEST_BODY);
            if (hasAnnotation) {
                map.put(MapKeyConstant.HAS_REQUEST_BODY, fieldName);
                return false;
            }
        }
        // 只要包含多个子节点都需要打散
        return FieldType.POJO.getType().equals(structureAndCommentInfo.getFieldTypeCode());
    }

}
