package cn.gudqs7.plugins.generate.getter.action;

import cn.gudqs7.plugins.common.util.structure.PsiMethodUtil;
import cn.gudqs7.plugins.generate.base.BaseGenerate;
import cn.gudqs7.plugins.generate.base.BaseGenerateAction;
import cn.gudqs7.plugins.generate.base.BaseVar;
import cn.gudqs7.plugins.generate.consant.GenerateConst;
import cn.gudqs7.plugins.generate.getter.GetterGenerate;
import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author wq
 */
@SuppressWarnings("IntentionDescriptionNotFoundInspection")
public class GetterGenerateAction extends BaseGenerateAction {

    @Override
    protected boolean checkVariableClass(PsiClass psiClass) {
        return PsiMethodUtil.checkClassHasValidGetter(psiClass);
    }

    @Override
    protected BaseGenerate buildGenerateByVar(BaseVar baseVar) {
        return new GetterGenerate(baseVar);
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
