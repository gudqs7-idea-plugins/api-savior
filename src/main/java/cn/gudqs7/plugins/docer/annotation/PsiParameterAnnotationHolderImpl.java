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
        CommentInfoTag commentInfo = new CommentInfoTag();
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
                                                commentInfo.setNotes(tagVal);
                                                break;
                                            default:
                                                String oldValue = commentInfo.getValue(null);
                                                if (oldValue != null) {
                                                    commentInfo.setValue(oldValue + t);
                                                } else {
                                                    commentInfo.setValue(t);
                                                }
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

        dealOtherAnnotation(commentInfo);
        return commentInfo;
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
        dealOtherAnnotation(commentInfo);
        return commentInfo;
    }


    private void dealOtherAnnotation(CommentInfo commentInfo) {
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
    private  <T> T getAnnotationValueByReqParam(String attr) {
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
