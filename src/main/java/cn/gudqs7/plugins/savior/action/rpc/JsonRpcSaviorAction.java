package cn.gudqs7.plugins.savior.action.rpc;

import cn.gudqs7.plugins.common.base.action.AbstractOnRightClickSavior;
import cn.gudqs7.plugins.savior.savior.more.JavaToJsonRpcCurlSavior;
import cn.gudqs7.plugins.savior.theme.ThemeHelper;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;

/**
 * @author Seayon
 * @BelongProjecet docer-savior-idea-plugin
 * @BelongPackage cn.gudqs7.plugins.savior.action.rpc
 * @Copyleft 2013-3102
 * @Date 2022/9/23 16:42
 * @Description
 */

public class JsonRpcSaviorAction extends AbstractOnRightClickSavior {
    private JavaToJsonRpcCurlSavior javaToJsonRpcCurlSavior = new JavaToJsonRpcCurlSavior(ThemeHelper.getRpcTheme());

    /**
     * Json RPC 接口上都有的注解
     */
    public static final String JSON_RPC_SERVICE_ANNOTATION = "com.googlecode.jsonrpc4j.JsonRpcService";

    @Override
    protected void checkPsiMethod(PsiMethod psiMethod, Project project, AnActionEvent e) {
        if (psiMethod!=null&&psiMethod.getContainingClass()!=null && psiMethod.getContainingClass().getAnnotation(JSON_RPC_SERVICE_ANNOTATION) == null) {
            notVisible(e);
        }
    }

    @Override
    protected void checkPsiClass(PsiClass psiClass, Project project, AnActionEvent e) {
        // 在类上右击时都不显示
        notVisible(e);
    }

    @Override
    protected String handlePsiMethod0(Project project, PsiMethod psiMethod, String psiClassName) {
        return javaToJsonRpcCurlSavior.generateJsonRpcCurl(project, psiClassName, psiMethod, false);
    }
}
