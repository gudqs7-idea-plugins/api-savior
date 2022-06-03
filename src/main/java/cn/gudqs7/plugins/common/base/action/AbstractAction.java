package cn.gudqs7.plugins.common.base.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.psi.*;

/**
 * @author wq
 * @date 2022/6/3
 */
public abstract class AbstractAction extends AnAction {

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

}
