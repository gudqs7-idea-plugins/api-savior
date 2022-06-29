package cn.gudqs7.plugins.common.pojo.resolver;

import cn.gudqs7.plugins.common.consts.CommonConst;
import cn.gudqs7.plugins.common.enums.MoreCommentTagEnum;
import cn.gudqs7.plugins.common.resolver.comment.AnnotationHolder;
import cn.gudqs7.plugins.common.util.structure.BaseTypeParseUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 字段注解/注释
 * 类上注释/注解
 * 方法上注释/注解
 *
 * @author wq
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommentInfo extends RequestMapping {

    /**
     * 父类
     */
    private AnnotationHolder parent;

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

    public String getNotes(String defaultVal) {
        if (StringUtils.isBlank(notes)) {
            return defaultVal;
        }
        return this.notes;
    }

    public String getExample(String defaultVal) {
        if (StringUtils.isBlank(example)) {
            return defaultVal;
        }
        return example;
    }

    public String getName(String defaultVal) {
        if (StringUtils.isBlank(name)) {
            return defaultVal;
        }
        return name;
    }

    public boolean isRequired(boolean defaultVal) {
        if (this.required == null) {
            return defaultVal;
        }
        return this.required;
    }

    public boolean isHidden(boolean defaultVal) {
        if (this.hidden == null) {
            return defaultVal;
        }
        return this.hidden;
    }

    public String getTags(String defaultVal) {
        if (StringUtils.isBlank(tags)) {
            return defaultVal;
        }
        return tags;
    }

    public List<String> getHiddenRequest() {
        return getSplitData(MoreCommentTagEnum.HIDDEN_REQUEST.getTag(), ",");
    }

    public List<String> getHiddenResponse() {
        return getSplitData(MoreCommentTagEnum.HIDDEN_RESPONSE.getTag(), ",");
    }

    public List<String> getOnlyRequest() {
        return getSplitData(MoreCommentTagEnum.ONLY_REQUEST.getTag(), ",");
    }

    public List<String> getOnlyResponse() {
        return getSplitData(MoreCommentTagEnum.ONLY_RESPONSE.getTag(), ",");
    }

    @NotNull
    public List<String> getSplitData(String tagKey, String splitRegex) {
        List<String> splitDataArray = new ArrayList<>();
        List<String> list = otherTagMap.get(tagKey);
        if (CollectionUtils.isNotEmpty(list)) {
            for (String item : list) {
                String[] itemArray = item.split(splitRegex);
                for (String item0 : itemArray) {
                    if (StringUtils.isNotBlank(item0)) {
                        splitDataArray.add(item0.trim());
                    }
                }
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

    public String getSingleStr(String tagKey, String defaultVal) {
        List<String> list = otherTagMap.get(tagKey);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0);
        }
        return defaultVal;
    }

    /**
     * 类的主名称, 可通过 #tags #description 等设置
     *
     * @param defaultName 默认值
     * @return 名称
     */
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

    public void appendValue(String value) {
        appendValue(value, CommonConst.BREAK_LINE);
    }

    public void appendValue(String value, String splitText) {
        String oldVal = this.value;
        if (StringUtils.isBlank(oldVal)) {
            this.value = value;
        } else {
            this.value += splitText + value;
        }
    }

    public void appendNotes(String notes) {
        String oldVal = this.notes;
        if (StringUtils.isBlank(oldVal)) {
            this.notes = notes;
        } else {
            this.notes += CommonConst.BREAK_LINE + notes;
        }
    }

    public void appendToTag(String tag, String tagVal) {
        List<String> list = getOtherTagMap().computeIfAbsent(tag, k -> new ArrayList<>());
        list.add(tagVal);
    }

}
