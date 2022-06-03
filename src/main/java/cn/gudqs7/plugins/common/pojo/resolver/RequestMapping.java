package cn.gudqs7.plugins.common.pojo.resolver;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @author wq
 */
@Data
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

    public String getMethod(String defaultVal) {
        if (StringUtils.isBlank(this.method)) {
            return defaultVal;
        }
        return this.method;
    }

    public String getContentType(String defaultVal) {
        if (StringUtils.isBlank(this.contentType)) {
            return defaultVal;
        }
        return this.contentType;
    }

    public interface ContentType {
        String APPLICATION_JSON = "applicatin/json";
        String X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
        String FORM_DATA = "form-data";
    }

    public interface Method {
        String POST = "POST";
        String GET = "GET";
        String PUT = "PUT";
        String DELETE = "DELETE";
    }

}
