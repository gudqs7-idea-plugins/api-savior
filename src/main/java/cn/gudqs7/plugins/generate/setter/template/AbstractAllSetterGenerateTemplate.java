package cn.gudqs7.plugins.generate.setter.template;

import cn.gudqs7.plugins.generate.base.AbstractVariableGenerateTemplate;
import cn.gudqs7.plugins.generate.base.BaseGenerate;
import cn.gudqs7.plugins.generate.base.BaseVar;
import cn.gudqs7.plugins.generate.setter.SetterGenerate;
import org.jetbrains.annotations.NotNull;

/**
 * @author WQ
 * @date 2021/10/1
 */
public abstract class AbstractAllSetterGenerateTemplate extends AbstractVariableGenerateTemplate {

    private final boolean generateDefaultVal;

    public AbstractAllSetterGenerateTemplate(boolean generateDefaultVal, String templateName, String example) {
        super(templateName, example);
        this.generateDefaultVal = generateDefaultVal;
    }

    @NotNull
    @Override
    protected BaseGenerate getGenerateByVar(BaseVar baseVar) {
        return new SetterGenerate(generateDefaultVal, baseVar);
    }
}
