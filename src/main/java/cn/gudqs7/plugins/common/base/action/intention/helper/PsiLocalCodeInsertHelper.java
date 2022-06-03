package cn.gudqs7.plugins.common.base.action.intention.helper;

import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLocalVariable;
import org.jetbrains.annotations.NotNull;

/**
 * @author wq
 * @date 2022/6/3
 */
public class PsiLocalCodeInsertHelper implements CodeInsertHelper<PsiLocalVariable>{

    @Override
    public boolean supportElement(PsiElement psiElement) {
        return psiElement instanceof PsiLocalVariable;
    }

    @Override
    public Integer getInsertOffset(@NotNull PsiLocalVariable psiElement) {
        PsiElement parent = psiElement.getParent();
        if (parent == null) {
            return null;
        }
        return parent.getTextOffset() + parent.getText().length();
    }

    @Override
    public String getPrefix(Document elementDocument, @NotNull PsiLocalVariable psiElement) {
        PsiElement parent = psiElement.getParent();
        if (parent == null) {
            return null;
        }
        return calculateSplitText(elementDocument, parent.getTextOffset());
    }

}
