package cn.gudqs7.plugins.generate.setter.action;

import cn.gudqs7.plugins.common.util.structure.PsiMethodUtil;
import cn.gudqs7.plugins.generate.base.BaseGenerate;
import cn.gudqs7.plugins.generate.base.BaseGenerateAction;
import cn.gudqs7.plugins.generate.base.BaseVar;
import cn.gudqs7.plugins.generate.setter.SetterGenerate;
import com.intellij.psi.PsiClass;

/**
 * @author wq
 */
public abstract class AbstractSetterGenerateAction extends BaseGenerateAction {

    private final boolean generateDefaultVal;

    public AbstractSetterGenerateAction(boolean generateDefaultVal) {
        this.generateDefaultVal = generateDefaultVal;
    }

    @Override
    protected boolean checkVariableClass(PsiClass psiClass) {
        return PsiMethodUtil.checkClassHasValidSetter(psiClass);
    }

    @Override
    protected BaseGenerate buildGenerateByVar(BaseVar baseVar) {
        return new SetterGenerate(generateDefaultVal, baseVar);
    }

}
