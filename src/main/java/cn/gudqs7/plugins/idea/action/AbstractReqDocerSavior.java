package cn.gudqs7.plugins.idea.action;

import cn.gudqs7.plugins.idea.savior.BaseSavior;
import cn.gudqs7.plugins.idea.savior.JavaToApiSavior;
import cn.gudqs7.plugins.idea.savior.JavaToJsonSavior;
import cn.gudqs7.plugins.idea.util.ActionUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractReqDocerSavior extends AnAction {

    private JavaToJsonSavior jsonSavior;
    private JavaToApiSavior apiSavior;

    public AbstractReqDocerSavior(JavaToJsonSavior jsonSavior, JavaToApiSavior apiSavior) {
        this.jsonSavior = jsonSavior;
        this.apiSavior = apiSavior;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        PsiElement psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);
        PsiClass psiClass = ActionUtil.getPsiClass(psiElement);

        if (psiClass == null) {
            e.getPresentation().setVisible(false);
            return;
        }

        if (psiClass.isInterface()) {
            e.getPresentation().setVisible(false);
            return;
        }
        PsiAnnotation psiAnnotation = psiClass.getAnnotation("org.springframework.stereotype.Controller");
        if (psiAnnotation == null) {
            psiAnnotation = psiClass.getAnnotation("org.springframework.web.bind.annotation.RestController");
        }
        // 若类是 Controller 则不显示
        if (psiAnnotation != null) {
            e.getPresentation().setVisible(false);
            return;
        }
        String presentableText = psiClass.getName();
        if (BaseSavior.isJavaBaseType(presentableText) || "Object".equals(presentableText)) {
            e.getPresentation().setVisible(false);
            return;
        }
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            Project project = e.getData(PlatformDataKeys.PROJECT);
            PsiElement psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);
            PsiClass psiClass = ActionUtil.getPsiClass(psiElement);

            if (psiClass == null) {
                return;
            }

            String qualifiedName = psiClass.getQualifiedName();
            PsiClassType psiClassType = PsiType.getTypeByName(qualifiedName, project, GlobalSearchScope.allScope(project));
            if (psiClassType instanceof PsiClassReferenceType) {
                PsiClassReferenceType classReferenceType = (PsiClassReferenceType) psiClassType;
                String java2json = jsonSavior.java2json(classReferenceType, project);
                String java2api = apiSavior.java2api(classReferenceType, project);
                apiSavior.setSysClipboardText(java2json);
                Messages.showMultilineInputDialog(project, "已自动的将示例复制到您的剪切板!\n您可以粘贴后再复制下面的参数说明Markdown", "可以粘贴(Ctrl+V)了", java2api, Messages.getInformationIcon(), null);
            }

        } catch (Exception e1) {
            ActionUtil.handleException(e1);
        } finally {
            ActionUtil.emptyIp();
        }
    }

}
