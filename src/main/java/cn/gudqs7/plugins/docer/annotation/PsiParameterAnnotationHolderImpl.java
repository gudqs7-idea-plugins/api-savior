package cn.gudqs7.plugins.docer.annotation;

import cn.gudqs7.plugins.docer.constant.CommentTag;
import cn.gudqs7.plugins.docer.constant.MoreCommentTag;
import cn.gudqs7.plugins.docer.pojo.annotation.CommentInfo;
import cn.gudqs7.plugins.docer.pojo.annotation.CommentInfoTag;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.javadoc.PsiDocComment;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author wq
 */
public class PsiParameterAnnotationHolderImpl extends AbstractAnnotationHolder {

    private final PsiParameter psiParameter;

    public PsiParameterAnnotationHolderImpl(PsiParameter psiParameter) {
        this.psiParameter = psiParameter;
    }

    @Override
    public PsiAnnotation getAnnotationByQname(String qName) {
        return psiParameter.getAnnotation(qName);
    }

    @Override
    public CommentInfoTag getCommentInfoByComment() {
        CommentInfoTag commentInfoTag = new CommentInfoTag();
        PsiElement parent = psiParameter.getParent().getParent();
        String parameterName = psiParameter.getName();
        if (parent instanceof PsiMethod) {
            for (PsiElement child : parent.getChildren()) {
                if (child instanceof PsiDocComment) {
                    Map<String, CommentTag> commentTagMap = CommentTag.allTagMap();
                    Map<String, MoreCommentTag> moreCommentTagMap = MoreCommentTag.allTagMap();
                    PsiDocComment psiComment = (PsiDocComment) child;
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
                            if (line.contains("@")) {
                                String atParam = "@param";
                                if (line.startsWith("@param")) {
                                    line = line.substring(atParam.length()).trim();
                                    String[] paramInfoArray = line.split(" ");
                                    String fieldName = "";
                                    if (paramInfoArray.length > 0) {
                                        fieldName = paramInfoArray[0];
                                    }
                                    if (!fieldName.equals(parameterName)) {
                                        continue;
                                    }
                                    String tag = line.substring(fieldName.length()).trim();
                                    String[] tagArray = tag.split(" ");
                                    for (String t : tagArray) {
                                        String tagName = t;
                                        String tagVal = null;
                                        if (t.contains("=")) {
                                            String[] tagKeyVal = t.split("=");
                                            tagName = tagKeyVal[0];
                                            if (tagKeyVal.length > 1) {
                                                tagVal = tagKeyVal[1];
                                            }
                                        }
                                        for (String moreTag : MoreCommentTag.allTagList()) {
                                            if (moreTag.equals(tagName)) {
                                                commentInfoTag.appendToTag(moreTag, tagVal);
                                            }
                                        }
                                        if (commentTagMap.containsKey(tagName)) {
                                            switch (CommentTag.of(tagName)) {
                                                case REQUIRED:
                                                    commentInfoTag.setRequired(getBooleanVal(tagVal));
                                                    break;
                                                case HIDDEN:
                                                    commentInfoTag.setHidden(getBooleanVal(tagVal));
                                                    break;
                                                case IMPORTANT:
                                                    commentInfoTag.setImportant(getBooleanVal(tagVal));
                                                    break;
                                                case EXAMPLE:
                                                    commentInfoTag.setExample(tagVal);
                                                    break;
                                                case NOTES:
                                                    commentInfoTag.appendNotes(tagVal);
                                                    break;
                                                default:
                                                    break;
                                            }
                                        } else if (moreCommentTagMap.containsKey(tag)) {
                                            commentInfoTag.appendToTag(tag, tagVal);
                                        } else {
                                            commentInfoTag.appendValue(t);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }
        dealOtherAnnotation(commentInfoTag);
        return commentInfoTag;
    }

    @Override
    public CommentInfo getCommentInfoByAnnotation() {
        CommentInfo commentInfo = new CommentInfo();
        boolean hasParamAnnotatation = hasAnnotation(QNAME_OF_PARAM);
        if (hasParamAnnotatation) {
            commentInfo.setHidden(getAnnotationValueByParam(CommentTag.HIDDEN.getTag()));
            commentInfo.setRequired(getAnnotationValueByParam(CommentTag.REQUIRED.getTag()));
            commentInfo.setValue(getAnnotationValueByParam(CommentTag.DEFAULT.getTag()));
            commentInfo.setExample(getAnnotationValueByParam(CommentTag.EXAMPLE.getTag()));
        }
        dealOtherAnnotation(commentInfo);
        return commentInfo;
    }


    private void dealOtherAnnotation(CommentInfo commentInfo) {
        boolean hasReqParamAnnotation = hasAnnotation(QNAME_OF_REQ_PARAM);
        if (hasReqParamAnnotation) {
            String name = getAnnotationValueByReqParam("name");
            if (name == null) {
                name = getAnnotationValueByReqParam(CommentTag.DEFAULT.getTag());
            }
            commentInfo.setName(name);
            Boolean required = getAnnotationValueByReqParam(CommentTag.REQUIRED.getTag());
            if (required == null || required) {
                commentInfo.setRequired(true);
            }
        }
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
    private <T> T getAnnotationValueByReqParam(String attr) {
        return getAnnotationValueByQname(QNAME_OF_REQ_PARAM, attr);
    }

    /**
     * 获取注解中的信息
     *
     * @param attr 注解字段
     * @return 信息
     */
    protected <T> T getAnnotationValueByParam(String attr) {
        return getAnnotationValueByQname(QNAME_OF_PARAM, attr);
    }


    @Override
    protected boolean usingAnnotation() {
        return hasAnyOneAnnotation(QNAME_OF_PARAM);
    }
}
