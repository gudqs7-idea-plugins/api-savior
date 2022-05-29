package cn.gudqs7.plugins.docer.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 更多注释
 *
 * @author wq
 * @date 2022/5/29
 */
@Getter
public enum MoreCommentTag {

    /**
     * 更多注释
     */
    JSON_FORMAT("json"),
    DATE_FORMAT("date"),
    HIDDEN_REQUEST("hiddenRequest"),
    HIDDEN_RESPONSE("hiddenResponse"),
    ONLY_REQUEST("onlyRequest"),
    ONLY_RESPONSE("onlyResponse"),
    POSTMAN_NO_RESPONSE("noResponse", true),
    EXAMPLE_RANDOM("random", true),
    EXAMPLE_GUID("guid", true),
    ;

    private final String tag;
    private final boolean boolType;

    MoreCommentTag(java.lang.String tag) {
        this.tag = tag;
        this.boolType = false;
    }

    MoreCommentTag(String tag, boolean boolType) {
        this.tag = tag;
        this.boolType = boolType;
    }

    public static List<String> allTagList() {
        return Arrays.stream(values()).map(MoreCommentTag::getTag).collect(Collectors.toList());
    }

    public static Map<String, MoreCommentTag> allTagMap() {
        Map<String, MoreCommentTag> allTagMap = new HashMap<>(32);
        for (MoreCommentTag commentTag : values()) {
            allTagMap.put(commentTag.getTag(), commentTag);
        }
        return allTagMap;
    }

}
