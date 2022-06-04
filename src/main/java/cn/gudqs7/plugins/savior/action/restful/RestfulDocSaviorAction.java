package cn.gudqs7.plugins.savior.action.restful;

import cn.gudqs7.plugins.common.util.structure.PsiClassUtil;
import cn.gudqs7.plugins.savior.action.base.AbstractDocerSavior;
import cn.gudqs7.plugins.savior.theme.ThemeHelper;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;

/**
 * @author wq
 */
public class RestfulDocSaviorAction extends AbstractDocerSavior {

    public RestfulDocSaviorAction() {
        super(ThemeHelper.getRestfulTheme());
    }

    @Override
    protected void checkPsiMethod(PsiMethod psiMethod, Project project, AnActionEvent e) {
        if (methodNotHaveMapping(psiMethod)) {
            notVisible(e);
        }
    }

    @Override
    protected void checkPsiClass(PsiClass psiClass, Project project, AnActionEvent e) {
        // controller 或者 feign
        if (!PsiClassUtil.isControllerOrFeign(psiClass)) {
            notVisible(e);
        }
    }
}
