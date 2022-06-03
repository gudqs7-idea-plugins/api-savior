package cn.gudqs7.plugins.savior.pojo;

import lombok.Data;

/**
 * @author wenquan
 * @date 2022/4/14
 */
@Data
public class PostmanKvInfo {

    private String key;
    private String value;
    private String src;
    private String description;
    private String type;
    private boolean disabled;

}
