package cn.gudqs7.plugins.idea.annotation;

import cn.gudqs7.plugins.idea.pojo.annotation.ApiModelProperty;
import cn.gudqs7.plugins.idea.pojo.annotation.ApiModelPropertyTag;
import cn.gudqs7.plugins.idea.util.AnnotationHolder;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiTypeElement;
import com.intellij.psi.javadoc.PsiDocComment;
import org.apache.commons.lang3.StringUtils;

/**
 * @author wq
 */
public class PsiReturnTypeAnnotationHolderImpl implements AnnotationHolder {

    private PsiTypeElement returnTypeElement;

    public PsiReturnTypeAnnotationHolderImpl(PsiTypeElement returnTypeElement) {
        this.returnTypeElement = returnTypeElement;
    }

    @Override
    public PsiAnnotation getAnnotation(String qname) {
        PsiElement parent = returnTypeElement.getParent();
        if (parent instanceof PsiMethod) {
            PsiMethod psiMethod = (PsiMethod) parent;
            psiMethod.getAnnotation(qname);
        }
        return null;
    }

    @Override
    public ApiModelPropertyTag getApiModelPropertyByComment() {
        ApiModelPropertyTag apiModelPropertyTag = new ApiModelPropertyTag();
        PsiElement parent = returnTypeElement.getParent();
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
                                String atParam = "@return";
                                if (line.startsWith(atParam)) {
                                    line = line.substring(atParam.length()).trim();
                                    apiModelPropertyTag.setValue(line);
                                    break;
                                }
                            }

                        }
                    }
                    break;
                }
            }
        }
        return apiModelPropertyTag;
    }

    @Override
    public ApiModelProperty getApiModelPropertyByAnnotation() {
        ApiModelProperty apiModelProperty = new ApiModelProperty();
        return apiModelProperty;
    }


    @Override
    public ApiModelProperty getApiModelProperty() {
        return getApiModelPropertyByComment();
    }
}
