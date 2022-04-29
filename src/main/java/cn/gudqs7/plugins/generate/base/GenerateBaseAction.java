package cn.gudqs7.plugins.generate.base;

import cn.gudqs7.plugins.util.PsiDocumentUtil;
import cn.gudqs7.plugins.util.PsiUtil;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * @author wenquan
 * @date 2021/9/30
 */
public abstract class GenerateBaseAction extends PsiElementBaseIntentionAction {

    /**
     * 根据当前情况构建生成器
     * @param element 上下文信息
     * @return 生成器
     */
    protected GenerateBase buildGenerate(PsiElement element) {
        PsiLocalVariable psiLocal = PsiTreeUtil.getParentOfType(element, PsiLocalVariable.class);
        BaseVar baseVar = null;
        if (psiLocal != null) {
            baseVar = new BaseVar();
            baseVar.setVarName(psiLocal.getName());
            baseVar.setVarType(psiLocal.getType());
        }
        PsiParameter psiParameter = PsiTreeUtil.getParentOfType(element, PsiParameter.class);
        if (psiParameter != null) {
            baseVar = new BaseVar();
            baseVar.setVarName(psiParameter.getName());
            baseVar.setVarType(psiParameter.getType());
        }
        return buildGenerateByVar(baseVar);
    }

    /**
     * 根据当前情况构建生成器
     * @param baseVar 变量
     * @return 生成器
     */
    protected abstract GenerateBase buildGenerateByVar(BaseVar baseVar);

    private PsiClass getMethodParameterContainingClass(PsiElement element) {
        PsiParameter psiParent = PsiTreeUtil.getParentOfType(element, PsiParameter.class);
        if (psiParent == null) {
            return null;
        }
        return PsiTypesUtil.getPsiClass(psiParent.getType());
    }
    private PsiClass getLocalVariableContainingClass(@NotNull PsiElement element) {
        PsiLocalVariable psiLocal = PsiTreeUtil.getParentOfType(element, PsiLocalVariable.class);
        if (psiLocal == null) {
            return null;
        }
        if (!(psiLocal.getParent() instanceof PsiDeclarationStatement)) {
            return null;
        }
        return PsiTypesUtil.getPsiClass(psiLocal.getType());
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        GenerateBase generateBase = buildGenerate(element);
        // 只需确保该变量含有get方法即可
        PsiClass localVariableContainingClass = getLocalVariableContainingClass(element);
        if (localVariableContainingClass != null) {
            return checkVariableClass(localVariableContainingClass);
        }
        PsiClass methodParameterContainingClass = getMethodParameterContainingClass(element);
        if (methodParameterContainingClass != null) {
            return checkVariableClass(methodParameterContainingClass);
        }
        return false;
    }

    /**
     * 校验变量对应的 psiClass 是否适用于该动作(动作: Getter/Setter)
     *
     * @param psiClass psi 类
     * @return true: 显示该动作 false:不显示该动作
     */
    protected abstract boolean checkVariableClass(PsiClass psiClass);

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        GenerateBase generateBase = buildGenerate(element);
        PsiLocalVariable psiLocal = PsiTreeUtil.getParentOfType(element, PsiLocalVariable.class);
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        PsiFile containingFile = element.getContainingFile();
        Document document = psiDocumentManager.getDocument(containingFile);
        if (document == null) {
            return;
        }
        if (psiLocal != null) {
            PsiElement parent = psiLocal.getParent();
            String splitText = PsiDocumentUtil.calculateSplitText(document, parent.getTextOffset(), "");
            int textOffset = parent.getTextOffset() + parent.getText().length();
            generateBase.insertCodeByPsiType(document, psiDocumentManager, containingFile, psiLocal.getType(), psiLocal.getName(), splitText, textOffset);
        }
        PsiParameter psiParameter = PsiTreeUtil.getParentOfType(element, PsiParameter.class);
        if (psiParameter != null) {
            PsiElement parent = psiParameter.getParent();
            PsiElement parent0 = parent.getParent();
            if (parent0 instanceof PsiMethod) {
                PsiMethod psiMethod = (PsiMethod) parent0;
                String splitText = PsiDocumentUtil.calculateSplitText(document, psiMethod.getTextRange().getStartOffset(), "    ");
                int insertOffset = psiMethod.getBody().getTextOffset() + 1;
                generateBase.insertCodeByPsiType(document, psiDocumentManager, containingFile, psiParameter.getType(), psiParameter.getName(), splitText, insertOffset);
            }
            if (parent instanceof PsiForeachStatement) {
                PsiForeachStatement psiForeachStatement = (PsiForeachStatement) parent;
                String splitText = PsiDocumentUtil.calculateSplitText(document, psiForeachStatement.getTextOffset(), "    ");
                int insertOffset = psiForeachStatement.getBody().getTextOffset() + 1;
                generateBase.insertCodeByPsiType(document, psiDocumentManager, containingFile, psiParameter.getType(), psiParameter.getName(), splitText, insertOffset);
            }
        }
        PsiUtil.clearGeneric();
    }

}
