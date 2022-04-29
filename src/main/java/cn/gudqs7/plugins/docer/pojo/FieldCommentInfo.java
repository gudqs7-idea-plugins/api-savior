package cn.gudqs7.plugins.docer.pojo;

import lombok.Data;

/**
 * @author wq
 */
@Data
public class FieldCommentInfo {

    private String fieldDesc;

    private String example;

    private boolean required;

    public FieldCommentInfo() {
        this.fieldDesc = "";
        this.example = "";
    }

    public FieldCommentInfo(String example) {
        this.example = example;
        this.fieldDesc = "";
    }

    public FieldCommentInfo(String fieldDesc, String example) {
        this.fieldDesc = fieldDesc;
        this.example = example;
        this.required = false;
    }

    public FieldCommentInfo(String fieldDesc, String example, boolean required) {
        this.fieldDesc = fieldDesc;
        this.example = example;
        this.required = required;
    }

}
