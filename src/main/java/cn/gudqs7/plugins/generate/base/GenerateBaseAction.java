package cn.gudqs7.plugins.generate.base;

import cn.gudqs7.plugins.common.base.action.intention.AbstractEditorIntentionAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiTypesUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author wenquan
 * @date 2021/9/30
 */
public abstract class GenerateBaseAction extends AbstractEditorIntentionAction {

    /**
     * 根据当前情况构建生成器
     *
     * @param element 上下文信息
     * @return 生成器
     */
    protected GenerateBase buildGenerate(PsiElement element) {
        BaseVar baseVar = null;
        PsiLocalVariable psiLocal = PsiTreeUtil.getParentOfType(element, PsiLocalVariable.class);
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
     *
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
    protected boolean isAvailable0(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
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
    protected void invoke0(Project project, Editor editor, PsiElement element, Document elementDocument, PsiDocumentManager psiDocumentManager) {
        GenerateBase generateBase = buildGenerate(element);
        PsiLocalVariable psiLocal = PsiTreeUtil.getParentOfType(element, PsiLocalVariable.class);
        PsiFile containingFile = element.getContainingFile();
        if (psiLocal != null) {
            String splitText = getPrefixWithBreakLine(elementDocument, psiLocal);
            int textOffset = getInsertOffset(psiLocal);
            generateBase.insertCodeByPsiType(elementDocument, psiDocumentManager, containingFile, splitText, textOffset);
        }
        PsiParameter psiParameter = PsiTreeUtil.getParentOfType(element, PsiParameter.class);
        if (psiParameter != null) {
            PsiElement parent = psiParameter.getParent();
            PsiElement parent0 = parent.getParent();
            if (parent0 instanceof PsiMethod) {
                PsiMethod psiMethod = (PsiMethod) parent0;
                String splitText = getPrefixWithBreakLine(elementDocument, psiMethod);
                int insertOffset = getInsertOffset(psiMethod);
                generateBase.insertCodeByPsiType(elementDocument, psiDocumentManager, containingFile, splitText, insertOffset);
            }
            if (parent instanceof PsiForeachStatement) {
                PsiForeachStatement psiForeachStatement = (PsiForeachStatement) parent;
                String splitText = getPrefixWithBreakLine(elementDocument, psiForeachStatement);
                int insertOffset = getInsertOffset(psiForeachStatement);
                generateBase.insertCodeByPsiType(elementDocument, psiDocumentManager, containingFile, splitText, insertOffset);
            }
        }
    }

}
