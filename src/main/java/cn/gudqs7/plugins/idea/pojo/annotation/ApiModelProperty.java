package cn.gudqs7.plugins.idea.pojo.annotation;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 字段注解/注释
 * 类上注释/注解
 * 方法上注释/注解
 * @author wq
 */
public class ApiModelProperty extends RequestMapping {
    /**
     * 字段含义
     */
    private String value = "";
    /**
     * 字段更多说明
     */
    private String notes = "";
    /**
     * 字段示例
     */
    private String example = "";
    /**
     * 自定义字段标识
     */
    private String name = "";
    /**
     * 字段是否必须
     */
    private Boolean required;
    /**
     * 字段是否跳过
     */
    private Boolean hidden;

    private String tags;

    private List<ResponseCodeInfo> responseCodeInfoList = new ArrayList<>();

    private List<String> hiddenRequest = new ArrayList<>();
    private List<String> hiddenResponse = new ArrayList<>();

    public String getValue(String defaultVal) {
        if (StringUtils.isBlank(value)) {
            return defaultVal;
        }
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getNotes(String defaultVal) {
        if (StringUtils.isBlank(notes)) {
            return defaultVal;
        }
        return this.notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getExample(String defaultVal) {
        if (StringUtils.isBlank(example)) {
            return defaultVal;
        }
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public String getName(String defaultVal) {
        if (StringUtils.isBlank(name)) {
            return defaultVal;
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRequired(boolean defaultVal) {
        if (this.required == null) {
            return defaultVal;
        }
        return this.required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public boolean isHidden(boolean defaultVal) {
        if (this.hidden == null) {
            return defaultVal;
        }
        return this.hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public String getTags(String defaultVal) {
        if (StringUtils.isBlank(tags)) {
            return defaultVal;
        }
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public List<ResponseCodeInfo> getResponseCodeInfoList() {
        return responseCodeInfoList;
    }

    public void setResponseCodeInfoList(List<ResponseCodeInfo> responseCodeInfoList) {
        this.responseCodeInfoList = responseCodeInfoList;
    }

    public List<String> getHiddenRequest() {
        return hiddenRequest;
    }

    public void setHiddenRequest(List<String> hiddenRequest) {
        this.hiddenRequest = hiddenRequest;
    }

    public List<String> getHiddenResponse() {
        return hiddenResponse;
    }

    public void setHiddenResponse(List<String> hiddenResponse) {
        this.hiddenResponse = hiddenResponse;
    }
}
