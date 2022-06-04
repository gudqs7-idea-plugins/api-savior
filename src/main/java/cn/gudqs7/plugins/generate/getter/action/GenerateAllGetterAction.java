package cn.gudqs7.plugins.generate.getter.action;

import cn.gudqs7.plugins.common.util.structure.PsiMethodUtil;
import cn.gudqs7.plugins.generate.base.BaseVar;
import cn.gudqs7.plugins.generate.base.GenerateBase;
import cn.gudqs7.plugins.generate.base.GenerateBaseAction;
import cn.gudqs7.plugins.generate.consant.GenerateConst;
import cn.gudqs7.plugins.generate.getter.GenerateGetter;
import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author wq
 */
@SuppressWarnings("IntentionDescriptionNotFoundInspection")
public class GenerateAllGetterAction extends GenerateBaseAction {

    @Override
    protected boolean checkVariableClass(PsiClass psiClass) {
        return PsiMethodUtil.checkClassHasValidGetter(psiClass);
    }

    @Override
    protected GenerateBase buildGenerateByVar(BaseVar baseVar) {
        return new GenerateGetter(baseVar);
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return GenerateConst.GENERATE_GETTER;
    }

    @NotNull
    @Override
    public String getText() {
        return GenerateConst.GENERATE_GETTER;
    }

}
