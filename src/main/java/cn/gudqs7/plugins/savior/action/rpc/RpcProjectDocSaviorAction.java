package cn.gudqs7.plugins.savior.action.rpc;

import cn.gudqs7.plugins.common.util.jetbrain.PsiClassUtil;
import cn.gudqs7.plugins.savior.action.base.AbstractProjectDocerSavior;
import cn.gudqs7.plugins.savior.savior.more.JavaToDocSavior;
import cn.gudqs7.plugins.savior.theme.ThemeHelper;
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
