package cn.gudqs7.plugins.docer.pojo.annotation;

import cn.gudqs7.plugins.docer.savior.base.BaseSavior;
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
        this.code = BaseSavior.replaceMd(code);
        this.message = BaseSavior.replaceMd(message);
    }

}
