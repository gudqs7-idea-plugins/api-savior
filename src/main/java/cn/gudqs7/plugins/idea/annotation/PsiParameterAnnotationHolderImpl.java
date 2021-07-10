package cn.gudqs7.plugins.idea.annotation;

import cn.gudqs7.plugins.idea.pojo.annotation.ApiModelProperty;
import cn.gudqs7.plugins.idea.pojo.annotation.ApiModelPropertyTag;
import cn.gudqs7.plugins.idea.pojo.annotation.CommentTag;
import cn.gudqs7.plugins.idea.util.AnnotationHolder;
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
    public ApiModelPropertyTag getApiModelPropertyByComment() {
        ApiModelPropertyTag apiModelPropertyTag = new ApiModelPropertyTag();
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
                                            case CommentTag.PARAM_REQUIRED:
                                                apiModelPropertyTag.setRequired(getBooleanVal(tagVal));
                                                break;
                                            case CommentTag.PARAM_HIDDEN:
                                                apiModelPropertyTag.setHidden(getBooleanVal(tagVal));
                                                break;
                                            case CommentTag.PARAM_IMPORTANT:
                                                apiModelPropertyTag.setImportant(getBooleanVal(tagVal));
                                                break;
                                            case CommentTag.PARAM_EXAMPLE:
                                                apiModelPropertyTag.setExample(tagVal);
                                                break;
                                            case CommentTag.PARAM_NOTES:
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
    public ApiModelProperty getApiModelPropertyByAnnotation() {
        ApiModelProperty apiModelProperty = new ApiModelProperty();
        boolean hasParamAnnotatation = hasAnnotatation(QNAME_OF_PARAM);
        if (hasParamAnnotatation) {
            apiModelProperty.setHidden(getAnnotationValueByParam("hidden"));
            apiModelProperty.setRequired(getAnnotationValueByParam("required"));
            apiModelProperty.setValue(getAnnotationValueByParam("value"));
            apiModelProperty.setExample(getAnnotationValueByParam("example"));
        }
        dealRequestParam(apiModelProperty);
        return apiModelProperty;
    }

    private void dealRequestParam(ApiModelProperty apiModelProperty) {
        boolean hasReqParamAnnotatation = hasAnnotatation(QNAME_OF_REQ_PARAM);
        if (hasReqParamAnnotatation) {
            String name = getAnnotationValueByReqParam("name");
            if (name == null) {
                name = getAnnotationValueByReqParam("value");
            }
            apiModelProperty.setName(name);
            Boolean required = getAnnotationValueByReqParam("required");
            if (required != null && required) {
                apiModelProperty.setRequired(true);
            }
        }
    }

    @Override
    public ApiModelProperty getApiModelProperty() {
        ApiModelProperty apiModelProperty = new ApiModelProperty();
        boolean hasAnnotatation = hasAnyOneAnnotatation(QNAME_OF_PARAM);
        ApiModelPropertyTag apiModelPropertyByComment = getApiModelPropertyByComment();
        if (hasAnnotatation) {
            if (apiModelPropertyByComment.isImportant()) {
                apiModelProperty = apiModelPropertyByComment;
            } else {
                apiModelProperty = getApiModelPropertyByAnnotation();
            }
        } else {
            apiModelProperty = apiModelPropertyByComment;
        }
        return apiModelProperty;
    }
}
