package cn.gudqs7.plugins.idea.action;

import cn.gudqs7.plugins.idea.savior.JavaToDocSavior;
import cn.gudqs7.plugins.idea.util.ActionUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

/**
 * @author wq
 */
public abstract class AbstractDocerSavior extends AnAction {

    private JavaToDocSavior docSavior;

    public AbstractDocerSavior(JavaToDocSavior docSavior) {
        this.docSavior = docSavior;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        PsiElement psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);

        if (psiElement == null) {
            e.getPresentation().setVisible(false);
            return;
        }

        PsiClass psiClass = ActionUtil.getPsiClass(psiElement);
        PsiMethod psiMethod = ActionUtil.getPsiMethod(psiElement);

        boolean isRightClickOnClass = psiClass != null;
        boolean isRightClickOnMethod = psiMethod != null;

        if (isRightClickOnMethod) {
            return;
        }
        if (isRightClickOnClass) {
            if (psiClass.isInterface()) {
                String name = psiClass.getQualifiedName();
                if ("java.util.List".equals(name)
                        || "java.util.Map".equals(name)
                        || "java.util.Set".equals(name)
                ) {
                    e.getPresentation().setVisible(false);
                    return;
                }
            } else {
                PsiAnnotation psiAnnotation = psiClass.getAnnotation("org.springframework.stereotype.Controller");
                if (psiAnnotation == null) {
                    psiAnnotation = psiClass.getAnnotation("org.springframework.web.bind.annotation.RestController");
                }
                // 若类不是 Controller 则不显示
                if (psiAnnotation == null) {
                    e.getPresentation().setVisible(false);
                    return;
                }
            }
        }
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            Project project = e.getData(PlatformDataKeys.PROJECT);
            PsiElement psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);
            PsiClass psiClass = ActionUtil.getPsiClass(psiElement);
            PsiMethod psiMethod = ActionUtil.getPsiMethod(psiElement);

            boolean isRightClickOnClass = psiClass != null;
            boolean isRightClickOnMethod = psiMethod != null;

            if (isRightClickOnMethod) {
                String interfaceClassName = psiMethod.getContainingClass().getQualifiedName();
                String docByMethod = docSavior.generateDocByMethod(project, interfaceClassName, psiMethod);
                docSavior.setSysClipboardText(docByMethod);
                ActionUtil.showDialog(project, "已自动的将 Markdown 文档复制到您的剪切板!\n您可在此预览后再去粘贴!", docByMethod);
            }

            if (isRightClickOnClass) {
                String docByInterface = docSavior.generateApiByServiceInterface(psiClass, project);
                docSavior.setSysClipboardText(docByInterface);
                ActionUtil.showDialog(project, "已自动的将 Markdown 文档复制到您的剪切板!\n您可在此预览后再去粘贴!", docByInterface);
            }

        } catch (Exception e1) {
            ActionUtil.handleException(e1);
        } finally {
            ActionUtil.emptyIp();
        }
    }

}
