package cn.gudqs7.plugins.common.enums;

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
public enum MoreCommentTagEnum {

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
    MODULE("module"),

    AMP_ACTION_NAME("ActionName"),
    AMP_RW("Rw"),
    AMP_FIELD("AmpField"),
    AMP_DATA_SIZE("DataSize"),
    AMP_CURRENT_TAG("currentTag"),
    AMP_PID("AmpPid"),
    AMP_MOCK_VAL("mock-val"),
    AMP_MOCK_KEY("mock-key"),
    ;

    private final String tag;
    private final boolean boolType;

    MoreCommentTagEnum(String tag) {
        this.tag = tag;
        this.boolType = false;
    }

    MoreCommentTagEnum(String tag, boolean boolType) {
        this.tag = tag;
        this.boolType = boolType;
    }

    public static List<String> allTagList() {
        return Arrays.stream(values()).map(MoreCommentTagEnum::getTag).collect(Collectors.toList());
    }

    public static Map<String, MoreCommentTagEnum> allTagMap() {
        Map<String, MoreCommentTagEnum> allTagMap = new HashMap<>(32);
        for (MoreCommentTagEnum commentTag : values()) {
            allTagMap.put(commentTag.getTag(), commentTag);
        }
        return allTagMap;
    }

}
