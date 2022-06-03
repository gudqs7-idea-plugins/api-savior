package cn.gudqs7.plugins.savior.pojo;

import cn.gudqs7.plugins.common.pojo.resolver.CommentInfo;
import cn.gudqs7.plugins.common.pojo.resolver.StructureAndCommentInfo;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

/**
 * @author wq
 */
@Data
public class ComplexInfo implements CharSequence {

    private Object realVal;

    private FieldCommentInfo fieldCommentInfo;

    private CommentInfo commentInfo;

    private StructureAndCommentInfo structureAndCommentInfo;

    public ComplexInfo() {
    }

    public ComplexInfo(Object realVal, FieldCommentInfo fieldCommentInfo) {
        this.realVal = realVal;
        this.fieldCommentInfo = fieldCommentInfo;
    }

    public ComplexInfo(Object realVal, FieldCommentInfo fieldCommentInfo, CommentInfo commentInfo) {
        this.realVal = realVal;
        this.fieldCommentInfo = fieldCommentInfo;
        this.commentInfo = commentInfo;
    }

    @Override
    public int length() {
        return toString().length();
    }

    @Override
    public char charAt(int index) {
        return toString().charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return toString().subSequence(start, end);
    }

    @Override
    public @NotNull String toString() {
        if (realVal == null) {
            return "null";
        }
        return realVal.toString();
    }
}
