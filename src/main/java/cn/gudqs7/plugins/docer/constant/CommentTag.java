package cn.gudqs7.plugins.docer.constant;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 常规注释
 *
 * @author wq
 */
@Getter
public enum CommentTag {

    /**
     * 常规注释
     */
    DEFAULT("value"),
    HIDDEN("hidden", true),
    REQUIRED("required", true),
    EXAMPLE("example"),
    NOTES("notes"),
    TAGS("tags"),
    IMPORTANT("important", true),
    DESCRIPTION("description"),
    ;

    private final String tag;
    private final boolean boolType;

    CommentTag(String tag) {
        this.tag = tag;
        this.boolType = false;
    }

    CommentTag(String tag, Boolean boolType) {
        this.tag = tag;
        this.boolType = boolType;
    }

    public static CommentTag of(String tag) {
        for (CommentTag commentTag : values()) {
            if (commentTag.getTag().equals(tag)) {
                return commentTag;
            }
        }
        return CommentTag.DEFAULT;
    }

    public static Map<String, CommentTag> allTagMap() {
        Map<String, CommentTag> allTagMap = new HashMap<>(32);
        for (CommentTag commentTag : values()) {
            allTagMap.put(commentTag.getTag(), commentTag);
        }
        return allTagMap;
    }

}
