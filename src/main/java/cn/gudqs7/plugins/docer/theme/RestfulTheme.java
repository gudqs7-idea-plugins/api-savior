package cn.gudqs7.plugins.docer.theme;

import cn.gudqs7.plugins.docer.annotation.AnnotationHolder;
import cn.gudqs7.plugins.docer.constant.FieldType;
import cn.gudqs7.plugins.docer.constant.MapKeyConstant;
import cn.gudqs7.plugins.docer.constant.ThemeType;
import cn.gudqs7.plugins.docer.pojo.PostmanKvInfo;
import cn.gudqs7.plugins.docer.pojo.StructureAndCommentInfo;
import cn.gudqs7.plugins.docer.pojo.annotation.CommentInfo;
import cn.gudqs7.plugins.docer.pojo.annotation.RequestMapping;
import cn.gudqs7.plugins.docer.reader.Java2BulkReader;
import cn.gudqs7.plugins.docer.util.JsonUtil;
import cn.gudqs7.plugins.docer.util.RestfulUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wq
 */
public class RestfulTheme implements Theme {

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
        return "restful/Method.ftl";
    }

    @Override
    public String getFieldPath() {
        return "restful/field.ftl";
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
            dataByStr.put("jsonExample", "");
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
