package cn.gudqs7.plugins.generate.setter.action;

import cn.gudqs7.plugins.generate.base.BaseVar;
import cn.gudqs7.plugins.generate.base.GenerateBase;
import cn.gudqs7.plugins.generate.base.GenerateBaseAction;
import cn.gudqs7.plugins.generate.setter.GenerateSetter;
import cn.gudqs7.plugins.util.PsiClassUtil;
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