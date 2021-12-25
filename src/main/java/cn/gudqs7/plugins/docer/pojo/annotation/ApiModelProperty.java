package cn.gudqs7.plugins.docer.pojo.annotation;

import cn.gudqs7.plugins.docer.util.BaseTypeParseUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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

    private Map<String, List<String>> otherTagMap = new HashMap<>(16);

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

    public List<String> getHiddenRequest() {
        return getSplitData("hiddenRequest", ",");
    }

    public List<String> getHiddenResponse() {
        return getSplitData("hiddenResponse", ",");
    }

    public List<String> getOnlyRequest() {
        return getSplitData("onlyRequest", ",");
    }

    public List<String> getOnlyResponse() {
        return getSplitData("onlyResponse", ",");
    }

    public Map<String, List<String>> getOtherTagMap() {
        return otherTagMap;
    }

    public void setOtherTagMap(Map<String, List<String>> otherTagMap) {
        this.otherTagMap = otherTagMap;
    }

    @NotNull
    public List<String> getSplitData(String tagKey, String splitRegex) {
        List<String> splitDataArray = new ArrayList<>();
        List<String> list = otherTagMap.get(tagKey);
        if (CollectionUtils.isNotEmpty(list)) {
            for (String item : list) {
                String[] hiddenInfoArray = item.split(splitRegex);
                splitDataArray.addAll(Arrays.asList(hiddenInfoArray));
            }
        }
        return splitDataArray;
    }

    public Boolean getSingleBool(String tagKey, Boolean defaultVal) {
        List<String> list = otherTagMap.get(tagKey);
        if (CollectionUtils.isNotEmpty(list)) {
            String tagVal = list.get(0);
            return getBooleanVal(tagVal);
        }
        return defaultVal;
    }

    public String getItemName(String defaultName) {
        String itemName = defaultName;
        String tags = getTags("");
        if (StringUtils.isNotBlank(tags)) {
            itemName = tags;
        } else {
            String value = getValue("");
            if (StringUtils.isNotBlank(value)) {
                itemName = value;
            }
        }
        return itemName;
    }

    private boolean getBooleanVal(String tagVal) {
        boolean flag;
        if (StringUtils.isBlank(tagVal)) {
            flag = true;
        } else {
            flag = BaseTypeParseUtil.parseBoolean(tagVal, true);
        }
        return flag;
    }

}
