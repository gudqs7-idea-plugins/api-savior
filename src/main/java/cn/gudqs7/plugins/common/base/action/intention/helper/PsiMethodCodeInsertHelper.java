package cn.gudqs7.plugins.common.base.action.intention.helper;

import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

/**
 * @author wq
 * @date 2022/6/3
 */
public class PsiMethodCodeInsertHelper implements CodeInsertHelper<PsiMethod> {

    @Override
    public boolean supportElement(PsiElement psiElement) {
        return psiElement instanceof PsiMethod;
    }

    @Override
    public Integer getInsertOffset(@NotNull PsiMethod psiMethod) {
        PsiCodeBlock psiMethodBody = psiMethod.getBody();
        if (psiMethodBody == null) {
            return null;
        }
        return psiMethodBody.getTextOffset() + 1;
    }

    @Override
    public String getPrefix(Document elementDocument, @NotNull PsiMethod psiMethod) {
        return SPACE_TAB + calculateSplitText(elementDocument, psiMethod.getTextRange().getStartOffset());
    }
}
