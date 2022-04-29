package cn.gudqs7.plugins.docer.pojo.annotation;

import lombok.Data;

/**
 * @author wq
 */
@Data
public class ResponseCodeInfo {

    private String code;
    private String message;
    private String reason;

    public ResponseCodeInfo() {
    }

    public ResponseCodeInfo(String code, String message, String reason) {
        this.code = code;
        this.message = message;
        this.reason = reason;
    }

}
