package cn.gudqs7.plugins.docer.annotation;

import cn.gudqs7.plugins.docer.constant.CommentConst;
import cn.gudqs7.plugins.docer.constant.CommentTag;
import cn.gudqs7.plugins.docer.pojo.annotation.CommentInfo;
import cn.gudqs7.plugins.docer.pojo.annotation.CommentInfoTag;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import org.apache.commons.lang3.StringUtils;

/**
 * @author wq
 */
public class PsiFieldAnnotationHolderImpl extends AbstractAnnotationHolder {

    private final PsiField psiField;

    public PsiFieldAnnotationHolderImpl(PsiField psiField) {
        this.psiField = psiField;
    }

    @Override
    public PsiAnnotation getAnnotationByQname(String qName) {
        return psiField.getAnnotation(qName);
    }

    @Override
    public CommentInfoTag getCommentInfoByComment() {
        CommentInfoTag commentInfo = new CommentInfoTag();
        for (PsiElement child : psiField.getChildren()) {
            if (child instanceof PsiComment) {
                PsiComment psiComment = (PsiComment) child;
                String text = psiComment.getText();
                if (text.startsWith("/**") && text.endsWith("*/")) {
                    String[] lines = text.replaceAll("\r", "").split("\n");
                    for (String line : lines) {
                        if (line.contains("/**") || line.contains("*/")) {
                            continue;
                        }
                        line = line.replaceAll("\\*", "").trim();
                        if (StringUtils.isBlank(line)) {
                            continue;
                        }
                        if (line.contains("@") || line.contains("#")) {
                            String[] tagValArray = line.split(" ");
                            String tag = "";
                            String tagVal = null;
                            if (tagValArray.length > 0) {
                                tag = tagValArray[0].trim();
                            }
                            if (tagValArray.length > 1) {
                                tagVal = line.substring(tag.length()).trim();
                            }
                            tag = tag.substring(1);
                            switch (tag) {
                                case CommentTag.REQUIRED:
                                    commentInfo.setRequired(getBooleanVal(tagVal));
                                    break;
                                case CommentTag.HIDDEN:
                                    commentInfo.setHidden(getBooleanVal(tagVal));
                                    break;
                                case CommentTag.IMPORTANT:
                                    commentInfo.setImportant(getBooleanVal(tagVal));
                                    break;
                                case CommentTag.EXAMPLE:
                                    commentInfo.setExample(tagVal);
                                    break;
                                case CommentTag.NOTES:
                                    String notes = commentInfo.getNotes(null);
                                    if (notes != null) {
                                        commentInfo.setNotes(notes + CommentConst.BREAK_LINE + tagVal);
                                    } else {
                                        commentInfo.setNotes(tagVal);
                                    }
                                    break;
                                default:
                                    commentInfo.appendToTag(tag, tagVal);
                                    break;
                            }
                        } else {
                            commentInfo.appendValue(line);
                        }
                    }
                }
                break;
            }
        }
        dealOtherAnnotation(commentInfo);
        return commentInfo;
    }

    @Override
    public CommentInfo getCommentInfoByAnnotation() {
        CommentInfo commentInfo = new CommentInfo();
        boolean hasAnnotatation = hasAnnotation(QNAME_OF_PROPERTY);
        if (hasAnnotatation) {
            commentInfo.setHidden(getAnnotationValueByProperty(CommentTag.HIDDEN));
            commentInfo.setRequired(getAnnotationValueByProperty(CommentTag.REQUIRED));
            String value = getAnnotationValueByProperty(CommentTag.DEFAULT);
            String notes = getAnnotationValueByProperty(CommentTag.NOTES);
            if (StringUtils.isNotBlank(value)) {
                value = value.replaceAll("\\n", CommentConst.BREAK_LINE);
            }
            if (StringUtils.isNotBlank(notes)) {
                notes = notes.replaceAll("\\n", CommentConst.BREAK_LINE);
            }
            commentInfo.setValue(value);
            commentInfo.setNotes(notes);
            commentInfo.setExample(getAnnotationValueByProperty(CommentTag.EXAMPLE));
        }
        dealOtherAnnotation(commentInfo);
        return commentInfo;
    }

    private void dealOtherAnnotation(CommentInfo commentInfo) {
        // 处理日期注解
        handleDateFormatAnnotation(commentInfo);
        // 根据 @Valid 配置信息覆盖是否必填字段
        overrideRequiredByValid(commentInfo);
        // 往更多说明填充一些信息
        addInfoToNotes(commentInfo);
    }

    /**
     * 获取注解中的信息
     *
     * @param attr 注解字段
     * @return 信息
     */
    private  <T> T getAnnotationValueByProperty(String attr) {
        return getAnnotationValueByQname(QNAME_OF_PROPERTY, attr);
    }

    @Override
    protected boolean usingAnnotation() {
        return hasAnnotation(QNAME_OF_PROPERTY);
    }

}
