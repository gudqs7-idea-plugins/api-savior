package cn.gudqs7.plugins.common.enums;

/**
 * @author wq
 */

public enum HttpMethod {

    /**
     * http 方法
     */
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE"),
    PATCH("PATCH"),
    ALL("ALL")
    ;

    public String getMethod() {
        return method;
    }

    private final String method;

    HttpMethod(String method) {
        this.method = method;
    }

    public static HttpMethod of(String method) {
        for (HttpMethod httpMethod : values()) {
            if (httpMethod.getMethod().equals(method)) {
                return httpMethod;
            }
        }
        return GET;
    }
}

