package cn.gudqs7.plugins.docer.annotation;

import cn.gudqs7.plugins.docer.constant.CommentTag;
import cn.gudqs7.plugins.docer.pojo.annotation.CommentInfo;
import cn.gudqs7.plugins.docer.pojo.annotation.CommentInfoTag;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.javadoc.PsiDocComment;
import org.apache.commons.lang3.StringUtils;

/**
 * @author wq
 */
public class PsiParameterAnnotationHolderImpl implements AnnotationHolder {

    private PsiParameter psiParameter;

    public PsiParameterAnnotationHolderImpl(PsiParameter psiParameter) {
        this.psiParameter = psiParameter;
    }

    @Override
    public PsiAnnotation getAnnotation(String qname) {
        return psiParameter.getAnnotation(qname);
    }

    @Override
    public CommentInfoTag getCommentInfoByComment() {
        CommentInfoTag apiModelPropertyTag = new CommentInfoTag();
        PsiElement parent = psiParameter.getParent().getParent();
        String parameterName = psiParameter.getName();
        if (parent instanceof PsiMethod) {
            for (PsiElement child : parent.getChildren()) {
                if (child instanceof PsiDocComment) {
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
                                        switch (tagName) {
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
                                                apiModelPropertyTag.setNotes(tagVal);
                                                break;
                                            default:
                                                apiModelPropertyTag.setValue(t);
                                                break;
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

        dealRequestParam(apiModelPropertyTag);
        return apiModelPropertyTag;
    }

    @Override
    public CommentInfo getCommentInfoByAnnotation() {
        CommentInfo commentInfo = new CommentInfo();
        boolean hasParamAnnotatation = hasAnnotation(QNAME_OF_PARAM);
        if (hasParamAnnotatation) {
            commentInfo.setHidden(getAnnotationValueByParam("hidden"));
            commentInfo.setRequired(getAnnotationValueByParam("required"));
            commentInfo.setValue(getAnnotationValueByParam("value"));
            commentInfo.setExample(getAnnotationValueByParam("example"));
        }
        dealRequestParam(commentInfo);
        return commentInfo;
    }

    private void dealRequestParam(CommentInfo commentInfo) {
        boolean hasReqParamAnnotation = hasAnnotation(QNAME_OF_REQ_PARAM);
        if (hasReqParamAnnotation) {
            String name = getAnnotationValueByReqParam("name");
            if (name == null) {
                name = getAnnotationValueByReqParam("value");
            }
            commentInfo.setName(name);
            Boolean required = getAnnotationValueByReqParam("required");
            if (required != null && required) {
                commentInfo.setRequired(true);
            }
        }
    }

    @Override
    public CommentInfo getCommentInfo() {
        CommentInfo commentInfo = new CommentInfo();
        boolean hasAnnotatation = hasAnyOneAnnotation(QNAME_OF_PARAM);
        CommentInfoTag apiModelPropertyByComment = getCommentInfoByComment();
        if (hasAnnotatation) {
            if (apiModelPropertyByComment.isImportant()) {
                commentInfo = apiModelPropertyByComment;
            } else {
                commentInfo = getCommentInfoByAnnotation();
            }
        } else {
            commentInfo = apiModelPropertyByComment;
        }
        commentInfo.setParent(this);
        return commentInfo;
    }
}
