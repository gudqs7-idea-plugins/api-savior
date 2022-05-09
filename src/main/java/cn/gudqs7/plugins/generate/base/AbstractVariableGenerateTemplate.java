package cn.gudqs7.plugins.generate.base;

import com.intellij.openapi.editor.Document;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

/**
 * @author WQ
 * @date 2021/10/6
 */
public abstract class AbstractVariableGenerateTemplate extends GenerateBaseTemplate {

    public AbstractVariableGenerateTemplate(String templateName, String example) {
        super(templateName, example, AbstractVariableGenerateTemplate::isApplicable0);
    }

    private static boolean isApplicable0(PsiElement psiElement) {
        if (psiElement == null) {
            return false;
        }
        if (psiElement instanceof PsiIdentifier) {
            PsiElement parent = psiElement.getParent();
            if (parent instanceof PsiReferenceExpression) {
                PsiReferenceExpression expression = (PsiReferenceExpression) parent;
                if (expression.getType() != null) {
                    return true;
                }
            }
        }
        if (psiElement instanceof PsiExpression) {
            PsiExpression psiExpression = (PsiExpression) psiElement;
            PsiType psiType = psiExpression.getType();
            return psiType != null;
        }
        return false;
    }

    @Override
    protected GenerateBase buildGenerate(PsiElement psiElement, PsiFile containingFile, PsiDocumentManager psiDocumentManager, Document document) {
        if (psiElement instanceof PsiExpression) {
            PsiExpression psiExpression = (PsiExpression) psiElement;
            PsiType psiType = psiExpression.getType();
            String varName = psiExpression.getText();
            if (psiType != null) {
                BaseVar baseVar = new BaseVar();
                baseVar.setVarName(varName);
                baseVar.setVarType(psiType);
                return getGenerateByVar(baseVar);
            }
        }
        return null;
    }

    /**
     * 根据 var 构建
     *
     * @param baseVar var
     * @return 生成器
     */
    @NotNull
    protected abstract GenerateBase getGenerateByVar(BaseVar baseVar);

}
