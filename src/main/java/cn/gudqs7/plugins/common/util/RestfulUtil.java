package cn.gudqs7.plugins.common.util;

import cn.gudqs7.plugins.savior.docer.pojo.PostmanKvInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author wenquan
 * @date 2022/4/15
 */
public class RestfulUtil {


    public static String getFirstMethod(String method) {
        String method0 = method;
        if (method0.contains("/")) {
            method0 = method0.substring(0, method0.indexOf("/"));
        }
        return method0;
    }

    public static String getUrlQuery(List<PostmanKvInfo> queryList, Boolean onlyRequire) {
        // 生成 ? 后面参数
        if (CollectionUtils.isNotEmpty(queryList)) {
            StringBuilder sbf = new StringBuilder("?");
            for (PostmanKvInfo postmanKvInfo : queryList) {
                if (postmanKvInfo.isDisabled()) {
                    if (onlyRequire != null && onlyRequire) {
                        continue;
                    }
                }
                String value = postmanKvInfo.getValue();
                String src = postmanKvInfo.getSrc();
                if (StringUtils.isNotBlank(src)) {
                    // query 方式不支持文件, 跳过即可
                    continue;
                }
                boolean isDisabled = postmanKvInfo.isDisabled();
                sbf.append(postmanKvInfo.getKey()).append("=").append(value).append("&");
            }
            return sbf.substring(0, sbf.length() - 1);
        }
        return "";
    }

    public static String getPostmanBulkByKvList(List<PostmanKvInfo> kvList) {
        // bulk 格式
        if (CollectionUtils.isNotEmpty(kvList)) {
            StringBuilder sbf = new StringBuilder();
            for (PostmanKvInfo postmanKvInfo : kvList) {
                String key = postmanKvInfo.getKey();
                String value = postmanKvInfo.getValue();
                String src = postmanKvInfo.getSrc();
                String val = value;
                if (StringUtils.isNotBlank(src)) {
                    val = String.format("@\"%s\"", src);
                }
                if (postmanKvInfo.isDisabled()) {
                    sbf.append("//");
                }
                sbf.append(key).append(":").append(val).append("\n");
            }
            return sbf.toString();
        }
        return "";
    }

}
