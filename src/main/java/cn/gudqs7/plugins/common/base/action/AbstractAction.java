package cn.gudqs7.plugins.common.base.action;

import cn.gudqs7.plugins.common.util.WebEnvironmentUtil;
import cn.gudqs7.plugins.common.util.jetbrain.ExceptionUtil;
import cn.gudqs7.plugins.common.util.jetbrain.PsiTypeUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

/**
 * @author wq
 * @date 2022/6/3
 */
public abstract class AbstractAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        try {
            update0(e);
        } catch (Throwable ex) {
            ExceptionUtil.handleException(ex);
        } finally {
            destroy(e);
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            actionPerformed0(e);
        } catch (Throwable ex) {
            ExceptionUtil.handleException(ex);
        } finally {
            WebEnvironmentUtil.emptyIp();
            PsiTypeUtil.clearGeneric();
            destroy(e);
        }
    }

    protected void destroy(AnActionEvent e) {

    }

    /**
     * 显示前调用, 判断是否显示
     *
     * @param e 事件源对象
     */
    protected abstract void update0(@NotNull AnActionEvent e);

    /**
     * 动作触发时的逻辑代码
     *
     * @param e 事件源对象
     */
    protected abstract void actionPerformed0(@NotNull AnActionEvent e);

    protected PsiClass getPsiClass(PsiElement psiElement) {
        PsiClass psiClass = null;
        if (psiElement instanceof PsiClass) {
            psiClass = (PsiClass) psiElement;
        }
        if (psiElement instanceof PsiJavaFile) {
            PsiJavaFile psiJavaFile = (PsiJavaFile) psiElement;
            PsiClass[] classes = psiJavaFile.getClasses();
            for (PsiClass psiClass0 : classes) {
                PsiModifierList modifierList = psiClass0.getModifierList();
                if (modifierList != null) {
                    if (modifierList.hasModifierProperty(PsiModifier.PUBLIC)) {
                        return psiClass0;
                    }
                }
            }
        }
        return psiClass;
    }

    protected PsiMethod getPsiMethod(PsiElement psiElement) {
        PsiMethod psiMethod = null;
        if (psiElement instanceof PsiMethod) {
            psiMethod = (PsiMethod) psiElement;
        }
        return psiMethod;
    }

    protected PsiDirectory getPsiDirectory(PsiElement psiElement) {
        PsiDirectory psiDirectory = null;
        if (psiElement instanceof PsiDirectory) {
            psiDirectory = (PsiDirectory) psiElement;
        }
        return psiDirectory;
    }

    protected void notVisible(@NotNull AnActionEvent e) {
        e.getPresentation().setVisible(false);
    }

}
