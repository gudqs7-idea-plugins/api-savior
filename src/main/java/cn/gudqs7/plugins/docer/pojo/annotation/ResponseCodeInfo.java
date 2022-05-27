package cn.gudqs7.plugins.docer.pojo.annotation;

import lombok.Data;

/**
 * @author wq
 */
@Data
public class ResponseCodeInfo {

    private String code;
    private String message;

    public ResponseCodeInfo() {
    }

    public ResponseCodeInfo(String code, String message) {
        this.code = code;
        this.message = message;
    }

}
