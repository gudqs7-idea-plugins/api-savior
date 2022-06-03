package cn.gudqs7.plugins.common.base.action.intention.helper;

import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiForeachStatement;
import com.intellij.psi.PsiStatement;
import org.jetbrains.annotations.NotNull;

/**
 * @author wq
 * @date 2022/6/3
 */
public class PsiForeachCodeInsertHelper implements CodeInsertHelper<PsiForeachStatement> {

    @Override
    public boolean supportElement(PsiElement psiElement) {
        return psiElement instanceof PsiForeachStatement;
    }

    @Override
    public Integer getInsertOffset(@NotNull PsiForeachStatement psiForeachStatement) {
        PsiStatement foreachStatementBody = psiForeachStatement.getBody();
        if (foreachStatementBody == null) {
            return null;
        }
        return foreachStatementBody.getTextOffset() + 1;
    }

    @Override
    public String getPrefix(Document elementDocument, @NotNull PsiForeachStatement psiForeachStatement) {
        return SPACE_TAB + calculateSplitText(elementDocument, psiForeachStatement.getTextOffset());
    }
}
