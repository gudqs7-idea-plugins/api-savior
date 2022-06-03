package cn.gudqs7.plugins.common.resolver.comment;

import cn.gudqs7.plugins.common.pojo.resolver.CommentInfo;
import cn.gudqs7.plugins.common.pojo.resolver.CommentInfoTag;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiTypeElement;
import com.intellij.psi.javadoc.PsiDocComment;
import org.apache.commons.lang3.StringUtils;

/**
 * @author wq
 */
public class PsiReturnTypeAnnotationHolderImpl extends AbstractAnnotationHolder {

    private final PsiTypeElement returnTypeElement;

    public PsiReturnTypeAnnotationHolderImpl(PsiTypeElement returnTypeElement) {
        this.returnTypeElement = returnTypeElement;
    }

    @Override
    public PsiAnnotation getAnnotationByQname(String qName) {
        PsiElement parent = returnTypeElement.getParent();
        if (parent instanceof PsiMethod) {
            PsiMethod psiMethod = (PsiMethod) parent;
            psiMethod.getAnnotation(qName);
        }
        return null;
    }

    @Override
    public CommentInfoTag getCommentInfoByComment() {
        CommentInfoTag commentInfoTag = new CommentInfoTag();
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
                                    commentInfoTag.setValue(line);
                                    break;
                                }
                            }

                        }
                    }
                    break;
                }
            }
        }
        return commentInfoTag;
    }

    @Override
    public CommentInfo getCommentInfoByAnnotation() {
        return new CommentInfo();
    }

    @Override
    protected boolean usingAnnotation() {
        return false;
    }

}
