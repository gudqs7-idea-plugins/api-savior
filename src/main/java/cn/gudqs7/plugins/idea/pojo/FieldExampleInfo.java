package cn.gudqs7.plugins.idea.pojo;

/**
 * @author wq
 */
public class FieldExampleInfo {

    private String fieldDesc;

    private String example;

    private boolean required;

    public FieldExampleInfo() {
        this.fieldDesc = "";
        this.example = "";
    }

    public FieldExampleInfo(String fieldDesc, String example) {
        this.fieldDesc = fieldDesc;
        this.example = example;
        this.required = false;
    }

    public FieldExampleInfo(String fieldDesc, String example, boolean required) {
        this.fieldDesc = fieldDesc;
        this.example = example;
        this.required = required;
    }

    public String getFieldDesc() {
        return fieldDesc;
    }

    public void setFieldDesc(String fieldDesc) {
        this.fieldDesc = fieldDesc;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
