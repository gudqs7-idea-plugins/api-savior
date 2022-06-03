package cn.gudqs7.plugins.common.resolver.comment;

import cn.gudqs7.plugins.common.enums.CommentTagEnum;
import cn.gudqs7.plugins.common.enums.MoreCommentTagEnum;
import cn.gudqs7.plugins.common.pojo.resolver.CommentInfo;
import cn.gudqs7.plugins.common.pojo.resolver.CommentInfoTag;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * @author wq
 */
public class PsiClassAnnotationHolderImpl extends AbstractAnnotationHolder {

    private final PsiClass psiClass;

    public PsiClassAnnotationHolderImpl(PsiClass psiClass) {
        this.psiClass = psiClass;
    }

    @Override
    public PsiAnnotation getAnnotationByQname(String qName) {
        return psiClass.getAnnotation(qName);
    }

    @Override
    public CommentInfoTag getCommentInfoByComment() {
        CommentInfoTag commentInfoTag = new CommentInfoTag();
        for (PsiElement child : psiClass.getChildren()) {
            if (child instanceof PsiComment) {
                Map<String, CommentTagEnum> commentTagMap = CommentTagEnum.allTagMap();
                Map<String, MoreCommentTagEnum> moreCommentTagMap = MoreCommentTagEnum.allTagMap();
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
                            if (commentTagMap.containsKey(tag)) {
                                switch (CommentTagEnum.of(tag)) {
                                    case IMPORTANT:
                                        commentInfoTag.setImportant(getBooleanVal(tagVal));
                                        break;
                                    case NOTES:
                                        commentInfoTag.setNotes(tagVal);
                                        break;
                                    case TAGS:
                                        commentInfoTag.setTags(tagVal);
                                        break;
                                    case DESCRIPTION:
                                        commentInfoTag.appendValue(tagVal);
                                        break;
                                    case HIDDEN:
                                        commentInfoTag.setHidden(getBooleanVal(tagVal));
                                        break;
                                    default:
                                        break;
                                }
                            } else if (moreCommentTagMap.containsKey(tag)) {
                                commentInfoTag.appendToTag(tag, tagVal);
                            }
                        } else {
                            commentInfoTag.appendValue(line);
                        }
                    }
                }
                break;
            }
        }
        return commentInfoTag;
    }

    @Override
    public CommentInfo getCommentInfoByAnnotation() {
        CommentInfo commentInfo = new CommentInfo();
        if (hasAnnotation(QNAME_OF_MODEL)) {
            commentInfo.setValue(getAnnotationValueByQname(QNAME_OF_MODEL, CommentTagEnum.DEFAULT.getTag()));
        }
        if (hasAnnotation(QNAME_OF_API)) {
            commentInfo.setValue(getAnnotationValueByQname(QNAME_OF_API, CommentTagEnum.DESCRIPTION.getTag()));
            commentInfo.setNotes(getAnnotationValueByQname(QNAME_OF_API, CommentTagEnum.DESCRIPTION.getTag()));
            List<String> tagsList = getAnnotationListValueByQname(QNAME_OF_API, CommentTagEnum.TAGS.getTag());
            String tags = "";
            if (CollectionUtils.isNotEmpty(tagsList)) {
                tags = tagsList.get(0);
            }
            commentInfo.setTags(tags);
            commentInfo.setHidden(getAnnotationValueByQname(QNAME_OF_API, CommentTagEnum.HIDDEN.getTag()));
        }
        return commentInfo;
    }

    @Override
    protected boolean usingAnnotation() {
        return hasAnyOneAnnotation(QNAME_OF_MODEL, QNAME_OF_API);
    }

}
