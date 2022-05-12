package cn.gudqs7.plugins.docer.action.rpc;

import cn.gudqs7.plugins.docer.action.base.AbstractProjectDocerSavior;
import cn.gudqs7.plugins.docer.savior.more.JavaToDocSavior;
import cn.gudqs7.plugins.docer.theme.ThemeHelper;
import cn.gudqs7.plugins.util.PsiClassUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;

/**
 * @author wq
 */
public class RpcProjectDocSaviorAction extends AbstractProjectDocerSavior {

    public RpcProjectDocSaviorAction() {
        super(new JavaToDocSavior(ThemeHelper.getRpcTheme()));
    }

    @Override
    protected boolean isNeedDealPsiClass(PsiClass psiClass, Project project) {
        return PsiClassUtil.isNormalInterface(psiClass, project);
    }

    @Override
    protected String getDirPrefix() {
        return "rpc";
    }
}
