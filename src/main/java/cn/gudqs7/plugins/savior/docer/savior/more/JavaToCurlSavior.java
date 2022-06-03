package cn.gudqs7.plugins.savior.docer.savior.more;

import cn.gudqs7.plugins.common.util.JsonUtil;
import cn.gudqs7.plugins.common.util.RestfulUtil;
import cn.gudqs7.plugins.savior.docer.constant.MapKeyConstant;
import cn.gudqs7.plugins.savior.docer.pojo.PostmanKvInfo;
import cn.gudqs7.plugins.savior.docer.pojo.StructureAndCommentInfo;
import cn.gudqs7.plugins.savior.docer.pojo.annotation.CommentInfo;
import cn.gudqs7.plugins.savior.docer.pojo.annotation.RequestMapping;
import cn.gudqs7.plugins.savior.docer.reader.Java2BulkReader;
import cn.gudqs7.plugins.savior.docer.savior.base.AbstractSavior;
import cn.gudqs7.plugins.savior.docer.theme.Theme;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wq
 * @date 2021/5/19
 */
public class JavaToCurlSavior extends AbstractSavior<String> {

    private final Java2BulkReader java2BulkReader;

    public JavaToCurlSavior(Theme theme) {
        super(theme);
        java2BulkReader = new Java2BulkReader(theme);
    }

    public String generateCurl(Project project, String interfaceClassName, PsiMethod publicMethod, boolean onlyRequire) {
        Map<String, Object> param = new HashMap<>(2);
        param.put("onlyRequire", onlyRequire);
        String curl = getDataByMethod(project, interfaceClassName, publicMethod, param, true);
        if (curl == null) {
            return "";
        }
        return curl;
    }

    @Override
    protected String getDataByStructureAndCommentInfo(Project project, PsiMethod publicMethod, CommentInfo commentInfo, String interfaceClassName, StructureAndCommentInfo paramStructureAndCommentInfo, StructureAndCommentInfo returnStructureAndCommentInfo, Map<String, Object> param) {
        String url = commentInfo.getUrl("");
        String contentType = commentInfo.getContentType(theme.getDefaultContentType());
        String method = commentInfo.getMethod("");
        String method0 = RestfulUtil.getFirstMethod(method);
        boolean firstMethodIsGet = RequestMapping.Method.GET.equals(method0);

        HashMap<String, Object> data = new HashMap<>(2);
        data.put("removeRequestBody", true);
        List<PostmanKvInfo> queryList = java2BulkReader.read(paramStructureAndCommentInfo, data);
        List<PostmanKvInfo> kvList = java2BulkReader.read(paramStructureAndCommentInfo);

        Boolean onlyRequire = (Boolean) param.get("onlyRequire");

        String query = RestfulUtil.getUrlQuery(queryList, onlyRequire);

        String curl = "";
        if (firstMethodIsGet) {
            // 纯 GET, 参数拼接到 url 后面
            curl = String.format("curl --location --request GET '%s'", url + query);
        } else {
            switch (contentType) {
                case RequestMapping.ContentType.APPLICATION_JSON:
                    // POST + requestBody
                    String raw = getRaw(paramStructureAndCommentInfo, onlyRequire);
                    curl = String.format("curl --location --request POST '%s' --header 'Content-Type: application/json' --data-raw '%s'", url + query, raw);
                    break;
                case RequestMapping.ContentType.FORM_DATA:
                    // POST + form-data
                    String formData = getFromData(kvList, onlyRequire);
                    curl = String.format("curl --location --request POST '%s'%s", url, formData);
                    break;
                case RequestMapping.ContentType.X_WWW_FORM_URLENCODED:
                    // POST + x-www-form-data
                    String urlEncodeData = getUrlEncodeData(kvList, onlyRequire);
                    curl = String.format("curl --location --request POST '%s' --header 'Content-Type: application/x-www-form-urlencoded'%s", url, urlEncodeData);
                    break;
                default:
                    break;
            }
        }

        return curl;
    }

    private String getRaw(StructureAndCommentInfo paramStructureAndCommentInfo, Boolean onlyRequire) {
        HashMap<String, Object> data = new HashMap<>(2);
        data.put("onlyRequire", onlyRequire);
        Map<String, Object> java2jsonMap = java2JsonReader.read(paramStructureAndCommentInfo, data);
        Object key = java2jsonMap.get(MapKeyConstant.HAS_REQUEST_BODY);
        if (key instanceof String) {
            String key0 = (String) key;
            return JsonUtil.toJson(java2jsonMap.get(key0));
        }
        return null;
    }

    private String getUrlEncodeData(List<PostmanKvInfo> kvList, Boolean onlyRequire) {
        // --data-urlencode 'modelId=123' --data-urlencode 'callerType=customer' --data-urlencode 'callerUid=44404'
        // 生成格式如上的字符串
        return getStrByKvList(kvList, " --data-urlencode ", onlyRequire);
    }

    private String getFromData(List<PostmanKvInfo> kvList, Boolean onlyRequire) {
        // --form 'file=@"/D:/wq-develop/code/aliyun-aiops/pom.xml"' --form 'stsToken.accessKeyId="ak124"' --form 'stsToken.accessKeySecret="sk21"'
        // 生成格式如上的字符串
        return getStrByKvList(kvList, " --form ", onlyRequire);
    }

    private String getStrByKvList(List<PostmanKvInfo> kvList, String prefix, Boolean onlyRequire) {
        if (CollectionUtils.isNotEmpty(kvList)) {
            StringBuilder sbf = new StringBuilder();
            for (PostmanKvInfo postmanKvInfo : kvList) {
                if (postmanKvInfo.isDisabled()) {
                    if (onlyRequire != null && onlyRequire) {
                        continue;
                    }
                }
                String key = postmanKvInfo.getKey();
                String value = postmanKvInfo.getValue();
                String src = postmanKvInfo.getSrc();
                String val = value;
                if (StringUtils.isNotBlank(src)) {
                    val = String.format("@\"%s\"", src);
                }
                sbf.append(prefix)
                        .append("'")
                        .append(key)
                        .append("=")
                        .append(val)
                        .append("'");
            }
            return sbf.toString();
        }
        return "";
    }

}
