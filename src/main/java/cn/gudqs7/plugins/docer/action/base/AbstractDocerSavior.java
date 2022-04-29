package cn.gudqs7.plugins.docer.action.base;

import cn.gudqs7.plugins.docer.savior.more.JavaToDocSavior;
import cn.gudqs7.plugins.docer.theme.Theme;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;

/**
 * @author wenquan
 * @date 2022/4/13
 */
public abstract class AbstractDocerSavior extends AbstractOnRightClickSavior {

    private final JavaToDocSavior javaToDocSavior;

    public AbstractDocerSavior(Theme theme) {
        javaToDocSavior = new JavaToDocSavior(theme);
    }

    @Override
    protected void checkPsiMethod(PsiMethod psiMethod, Project project, AnActionEvent e) {

    }

    @Override
    protected void checkPsiClass(PsiClass psiClass, Project project, AnActionEvent e) {

    }

    @Override
    protected String handlePsiClass0(Project project, PsiClass psiClass) {
        return javaToDocSavior.generateApiByServiceInterface(psiClass, project);
    }

    @Override
    protected String handlePsiMethod0(Project project, PsiMethod psiMethod, String psiClassName) {
        return javaToDocSavior.generateDocByMethod(project, psiClassName, psiMethod, true);
    }

    @Override
    protected String getTip() {
        return "已自动的将 Markdown 文档复制到您的剪切板!\n您可在此预览后再去粘贴!";
    }

}
