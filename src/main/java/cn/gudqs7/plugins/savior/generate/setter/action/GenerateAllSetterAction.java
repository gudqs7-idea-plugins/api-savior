package cn.gudqs7.plugins.savior.generate.setter.action;

import cn.gudqs7.plugins.common.util.PsiClassUtil;
import cn.gudqs7.plugins.savior.generate.base.BaseVar;
import cn.gudqs7.plugins.savior.generate.base.GenerateBase;
import cn.gudqs7.plugins.savior.generate.base.GenerateBaseAction;
import cn.gudqs7.plugins.savior.generate.setter.GenerateSetter;
import com.intellij.psi.PsiClass;

/**
 * @author wq
 */
public abstract class GenerateAllSetterAction extends GenerateBaseAction {

    private final boolean generateDefaultVal;

    public GenerateAllSetterAction(boolean generateDefaultVal) {
        this.generateDefaultVal = generateDefaultVal;
    }

    @Override
    protected boolean checkVariableClass(PsiClass psiClass) {
        return PsiClassUtil.checkClassHasValidSetter(psiClass);
    }

    @Override
    protected GenerateBase buildGenerateByVar(BaseVar baseVar) {
        return new GenerateSetter(generateDefaultVal, baseVar);
    }

}
