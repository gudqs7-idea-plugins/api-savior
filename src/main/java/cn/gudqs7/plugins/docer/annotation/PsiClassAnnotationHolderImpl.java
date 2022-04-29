package cn.gudqs7.plugins.docer.annotation;

import cn.gudqs7.plugins.docer.constant.CommentTag;
import cn.gudqs7.plugins.docer.pojo.annotation.CommentInfo;
import cn.gudqs7.plugins.docer.pojo.annotation.CommentInfoTag;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wq
 */
public class PsiClassAnnotationHolderImpl implements AnnotationHolder {

    private PsiClass psiClass;

    public PsiClassAnnotationHolderImpl(PsiClass psiClass) {
        this.psiClass = psiClass;
    }

    @Override
    public PsiAnnotation getAnnotation(String qname) {
        return psiClass.getAnnotation(qname);
    }

    @Override
    public CommentInfoTag getCommentInfoByComment() {
        CommentInfoTag apiModelPropertyTag = new CommentInfoTag();
        for (PsiElement child : psiClass.getChildren()) {
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
                                case CommentTag.IMPORTANT:
                                    apiModelPropertyTag.setImportant(getBooleanVal(tagVal));
                                    break;
                                case CommentTag.NOTES:
                                    apiModelPropertyTag.setNotes(tagVal);
                                    break;
                                case CommentTag.TAGS:
                                    apiModelPropertyTag.setTags(tagVal);
                                    break;
                                case CommentTag.DESCRIPTION:
                                    String oldVal = apiModelPropertyTag.getValue("");
                                    if (StringUtils.isBlank(oldVal)) {
                                        apiModelPropertyTag.setValue(tagVal);
                                    }
                                    break;
                                case CommentTag.HIDDEN:
                                    apiModelPropertyTag.setHidden(getBooleanVal(tagVal));
                                    break;
                                default:
                                    List<String> list = apiModelPropertyTag.getOtherTagMap().computeIfAbsent(tag, k -> new ArrayList<>());
                                    list.add(tagVal);
                                    break;
                            }
                        } else {
                            String oldVal = apiModelPropertyTag.getValue("");
                            if (StringUtils.isBlank(oldVal)) {
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
        boolean hasAnnotatation = hasAnnotation(QNAME_OF_MODEL);
        if (hasAnnotatation) {
            commentInfo.setValue(getAnnotationValueByModel("value"));
        }
        hasAnnotatation = hasAnnotation(QNAME_OF_API);
        if (hasAnnotatation) {
            commentInfo.setValue(getAnnotationValueByQname(QNAME_OF_API, "description"));
            commentInfo.setNotes(getAnnotationValueByQname(QNAME_OF_API, "description"));
            List<String> tagsList = getAnnotationListValueByQname(QNAME_OF_API, "tags");
            String tags = "";
            if (CollectionUtils.isNotEmpty(tagsList)) {
                tags = tagsList.get(0);
            }
            commentInfo.setTags(tags);
            commentInfo.setHidden(getAnnotationValueByQname(QNAME_OF_API, "hidden"));
        }
        return commentInfo;
    }

    @Override
    public CommentInfo getCommentInfo() {
        CommentInfo commentInfo = new CommentInfo();
        boolean hasAnnotatation = hasAnnotation(QNAME_OF_MODEL);
        boolean hasApiAnnotatation = hasAnnotation(QNAME_OF_API);
        CommentInfoTag apiModelPropertyByComment = getCommentInfoByComment();
        if (hasAnnotatation || hasApiAnnotatation) {
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
