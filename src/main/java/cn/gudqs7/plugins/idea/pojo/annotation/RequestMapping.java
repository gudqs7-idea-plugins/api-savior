package cn.gudqs7.plugins.idea.pojo.annotation;

import org.apache.commons.lang3.StringUtils;

/**
 * @author wq
 */
public class RequestMapping {

    private String url;

    private String method;

    private String contentType;

    public String getUrl(String defaultVal) {
        if (StringUtils.isBlank(this.url)) {
            return defaultVal;
        }
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod(String defaultVal) {
        if (StringUtils.isBlank(this.method)) {
            return defaultVal;
        }
        return this.method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getContentType(String defaultVal) {
        if (StringUtils.isBlank(this.contentType)) {
            return defaultVal;
        }
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public static interface ContentType {
        String APPLICATION_JSON = "applicatin/json";
        String FORM_DATA = "application/x-www-form-urlencoded";
    }

    public static interface Method {
        String POST = "POST";
        String GET = "GET";
        String PUT = "PUT";
        String DELETE = "DELETE";
    }

}
