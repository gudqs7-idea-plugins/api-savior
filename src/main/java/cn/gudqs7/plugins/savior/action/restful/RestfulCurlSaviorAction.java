package cn.gudqs7.plugins.savior.action.restful;

import cn.gudqs7.plugins.common.base.action.AbstractOnRightClickSavior;
import cn.gudqs7.plugins.savior.savior.more.JavaToCurlSavior;
import cn.gudqs7.plugins.savior.theme.ThemeHelper;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;

/**
 * @author wenquan
 * @date 2022/4/13
 */
public class RestfulCurlSaviorAction extends AbstractOnRightClickSavior {

    protected final JavaToCurlSavior javaToCurlSavior;

    public RestfulCurlSaviorAction() {
        javaToCurlSavior = new JavaToCurlSavior(ThemeHelper.getRestfulTheme());
    }

    @Override
    protected void checkPsiMethod(PsiMethod psiMethod, Project project, AnActionEvent e) {
        if (methodNotHaveMapping(psiMethod)) {
            notVisible(e);
        }
    }

    @Override
    protected void checkPsiClass(PsiClass psiClass, Project project, AnActionEvent e) {
        // 忽视所有类
        notVisible(e);
    }

    @Override
    protected String handlePsiMethod0(Project project, PsiMethod psiMethod, String psiClassName) {
        return javaToCurlSavior.generateCurl(project, psiClassName, psiMethod, false);
    }

}
