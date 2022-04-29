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

import java.util.ArrayList;
import java.util.List;

/**
 * @author wq
 */
public class PsiFieldAnnotationHolderImpl implements AnnotationHolder {

    private PsiField psiField;

    public PsiFieldAnnotationHolderImpl(PsiField psiField) {
        this.psiField = psiField;
    }

    @Override
    public PsiAnnotation getAnnotation(String qname) {
        return psiField.getAnnotation(qname);
    }

    @Override
    public CommentInfoTag getCommentInfoByComment() {
        CommentInfoTag apiModelPropertyTag = new CommentInfoTag();
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
                                    apiModelPropertyTag.setRequired(getBooleanVal(tagVal));
                                    break;
                                case CommentTag.HIDDEN:
                                    apiModelPropertyTag.setHidden(getBooleanVal(tagVal));
                                    break;
                                case CommentTag.IMPORTANT:
                                    apiModelPropertyTag.setImportant(getBooleanVal(tagVal));
                                    break;
                                case CommentTag.EXAMPLE:
                                    apiModelPropertyTag.setExample(tagVal);
                                    break;
                                case CommentTag.NOTES:
                                    String notes = apiModelPropertyTag.getNotes(null);
                                    if (notes != null) {
                                        apiModelPropertyTag.setNotes(notes + CommentConst.BREAK_LINE + tagVal);
                                    } else {
                                        apiModelPropertyTag.setNotes(tagVal);
                                    }
                                    break;
                                default:
                                    List<String> list = apiModelPropertyTag.getOtherTagMap().computeIfAbsent(tag, k -> new ArrayList<>());
                                    list.add(tagVal);
                                    break;
                            }
                        } else {
                            String oldValue = apiModelPropertyTag.getValue(null);
                            if (oldValue != null) {
                                apiModelPropertyTag.setValue(oldValue + CommentConst.BREAK_LINE + line);
                            } else {
                                apiModelPropertyTag.setValue(line);
                            }
                        }
                    }
                }
                break;
            }
        }
        return apiModelPropertyTag;
    }

    @Override
    public CommentInfo getCommentInfoByAnnotation() {
        CommentInfo commentInfo = new CommentInfo();
        boolean hasAnnotatation = hasAnnotation(QNAME_OF_PROPERTY);
        if (hasAnnotatation) {
            commentInfo.setHidden(getAnnotationValueByProperty("hidden"));
            commentInfo.setRequired(getAnnotationValueByProperty("required"));
            String value = getAnnotationValueByProperty("value");
            String notes = getAnnotationValueByProperty("notes");
            if (StringUtils.isNotBlank(value)) {
                value = value.replaceAll("\\n", CommentConst.BREAK_LINE);
            }
            if (StringUtils.isNotBlank(notes)) {
                notes = notes.replaceAll("\\n", CommentConst.BREAK_LINE);
            }
            commentInfo.setValue(value);
            commentInfo.setNotes(notes);
            commentInfo.setExample(getAnnotationValueByProperty("example"));
        }
        return commentInfo;
    }

    @Override
    public CommentInfo getCommentInfo() {
        CommentInfo commentInfo = new CommentInfo();
        boolean hasAnnotatation = hasAnnotation(QNAME_OF_PROPERTY);
        CommentInfoTag apiModelPropertyByComment = getCommentInfoByComment();
        if (hasAnnotatation) {
            if (apiModelPropertyByComment.isImportant()) {
                commentInfo = apiModelPropertyByComment;
            } else {
                commentInfo = getCommentInfoByAnnotation();
                // 即使使用注解, 附加注释也会生效
                commentInfo.setOtherTagMap(apiModelPropertyByComment.getOtherTagMap());
            }
        } else {
            commentInfo = apiModelPropertyByComment;
        }
        commentInfo.setParent(this);
        return commentInfo;
    }

}
