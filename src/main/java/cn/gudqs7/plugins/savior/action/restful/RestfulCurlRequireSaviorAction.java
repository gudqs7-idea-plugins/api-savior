package cn.gudqs7.plugins.savior.action.restful;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;

/**
 * @author wenquan
 * @date 2022/4/13
 */
public class RestfulCurlRequireSaviorAction extends RestfulCurlSaviorAction {

    @Override
    protected String handlePsiMethod0(Project project, PsiMethod psiMethod, String psiClassName) {
        return javaToCurlSavior.generateCurl(project, psiClassName, psiMethod, true);
    }

}
