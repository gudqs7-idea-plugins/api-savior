package cn.gudqs7.plugins.docer.annotation;

import cn.gudqs7.plugins.docer.constant.CommentConst;
import cn.gudqs7.plugins.docer.constant.MoreCommentTag;
import cn.gudqs7.plugins.docer.pojo.annotation.CommentInfo;
import cn.gudqs7.plugins.docer.pojo.annotation.CommentInfoTag;
import cn.gudqs7.plugins.docer.util.BaseTypeParseUtil;
import cn.gudqs7.plugins.util.PsiAnnotationUtil;
import com.intellij.psi.PsiAnnotation;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 注解/注释获取-基类
 *
 * @author wenquan
 * @date 2022/5/27
 */
public abstract class AbstractAnnotationHolder implements AnnotationHolder {

    @Override
    public CommentInfo getCommentInfo() {
        CommentInfo commentInfo;
        CommentInfoTag commentInfoByComment = getCommentInfoByComment();
        if (usingAnnotation()) {
            if (commentInfoByComment.isImportant()) {
                commentInfo = commentInfoByComment;
            } else {
                commentInfo = getCommentInfoByAnnotation();
                // 即使使用注解, 附加注释也会生效
                commentInfo.setOtherTagMap(commentInfoByComment.getOtherTagMap());
            }
        } else {
            commentInfo = commentInfoByComment;
        }
        commentInfo.setParent(this);
        return commentInfo;
    }

    @Override
    public boolean hasAnnotation(String qName) {
        return getAnnotationByQname(qName) != null;
    }

    @Override
    public boolean hasAnyOneAnnotation(String... qNames) {
        for (String qName : qNames) {
            boolean hasAnnotation = getAnnotationByQname(qName) != null;
            if (hasAnnotation) {
                return true;
            }
        }
        return false;
    }

    /**
     * 使用注解还是注释信息为主
     *
     * @return true: 注解信息 false: 注释信息
     */
    protected abstract boolean usingAnnotation();

    /**
     * 获取注解中的信息
     *
     * @param qname 指定注解
     * @param attr  注解字段
     * @return 信息
     */
    protected <T> T getAnnotationValueByQname(String qname, String attr) {
        PsiAnnotation psiAnnotation = getAnnotationByQname(qname);
        return PsiAnnotationUtil.getAnnotationValue(psiAnnotation, attr, null);
    }

    /**
     * 获取注解中的信息(返回列表)
     *
     * @param qname 指定注解
     * @param attr  注解字段
     * @return 信息
     */
    protected <T> List<T> getAnnotationListValueByQname(String qname, String attr) {
        PsiAnnotation psiAnnotation = getAnnotationByQname(qname);
        return PsiAnnotationUtil.getAnnotationListValue(psiAnnotation, attr, null);
    }

    /**
     * 获取 bool 字符串的值, 不指定 false 则认为是 true
     *
     * @param tagVal bool 字符串
     * @return bool 值
     */
    protected boolean getBooleanVal(String tagVal) {
        boolean flag;
        if (StringUtils.isBlank(tagVal)) {
            flag = true;
        } else {
            flag = BaseTypeParseUtil.parseBoolean(tagVal, true);
        }
        return flag;
    }

    /**
     * 字段/参数增加更多信息
     *
     * @param commentInfo 原始数据
     */
    protected void addInfoToNotes(CommentInfo commentInfo) {
        String notes = commentInfo.getNotes("");
        String example = commentInfo.getExample("");
        if (StringUtils.isNotBlank(example)) {
            notes += "默认值为 " + example + CommentConst.BREAK_LINE;
        }
        boolean hasLengthAnnotation = hasAnnotation(QNAME_OF_VALID_LENGTH);
        if (hasLengthAnnotation) {
            Integer min = getAnnotationValueByQname(QNAME_OF_VALID_LENGTH, "min");
            Integer max = getAnnotationValueByQname(QNAME_OF_VALID_LENGTH, "max");
            if (min != null) {
                notes += "最小长度为 " + min + CommentConst.BREAK_LINE;
            }
            if (max != null) {
                notes += "最大长度为 " + max + CommentConst.BREAK_LINE;
            }
        }
        Long min = null;
        Long max = null;
        if (hasAnnotation(QNAME_OF_VALID_RANGE)) {
            min = getAnnotationValueByQname(QNAME_OF_VALID_RANGE, "min");
            max = getAnnotationValueByQname(QNAME_OF_VALID_RANGE, "min");
        }
        if (hasAnnotation(QNAME_OF_VALID_MIN)) {
            min = getAnnotationValueByQname(QNAME_OF_VALID_MIN, "value");
        }
        if (hasAnnotation(QNAME_OF_VALID_MAX)) {
            max = getAnnotationValueByQname(QNAME_OF_VALID_MAX, "value");
        }
        if (min != null) {
            notes += "最小值为 " + min + CommentConst.BREAK_LINE;
        }
        if (max != null) {
            notes += "最大值为 " + max + CommentConst.BREAK_LINE;
        }
        if (notes.endsWith(CommentConst.BREAK_LINE)) {
            notes = notes.substring(0, notes.length() - CommentConst.BREAK_LINE.length());
        }
        commentInfo.setNotes(notes);
    }

    /**
     * 根据 @Valid 相关注解覆盖是否必填信息
     *
     * @param commentInfo 原始信息
     */
    protected void overrideRequiredByValid(CommentInfo commentInfo) {
        boolean hasRequiredAnnotation = hasAnyOneAnnotation(QNAME_OF_VALID_NOT_NULL, QNAME_OF_VALID_NOT_EMPTY, QNAME_OF_VALID_NOT_BLANK);
        if (hasRequiredAnnotation) {
            commentInfo.setRequired(true);
        }
    }

    /**
     * 处理日期注解
     *
     * @param commentInfo 原始信息
     */
    protected void handleDateFormatAnnotation(CommentInfo commentInfo) {
        boolean hasJsonFormatAnnotation = hasAnnotation(QNAME_OF_JSON_FORMAT);
        if (hasJsonFormatAnnotation) {
            String pattern = getAnnotationValueByQname(QNAME_OF_JSON_FORMAT, "pattern");
            if (StringUtils.isNotBlank(pattern)) {
                commentInfo.appendToTag(MoreCommentTag.JSON_FORMAT.getTag(), pattern);
            }
        }
        boolean hasDateFormatAnnotation = hasAnnotation(QNAME_OF_DATE_TIME_FORMAT);
        if (hasDateFormatAnnotation) {
            String pattern = getAnnotationValueByQname(QNAME_OF_DATE_TIME_FORMAT, "pattern");
            if (StringUtils.isNotBlank(pattern)) {
                commentInfo.appendToTag(MoreCommentTag.DATE_FORMAT.getTag(), pattern);
            }
        }
    }

}
