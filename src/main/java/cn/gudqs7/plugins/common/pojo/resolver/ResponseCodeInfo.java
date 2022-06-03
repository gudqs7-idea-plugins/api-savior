package cn.gudqs7.plugins.common.pojo.resolver;

import cn.gudqs7.plugins.common.util.StringTool;
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
        this.code = StringTool.replaceMd(code);
        this.message = StringTool.replaceMd(message);
    }

}
