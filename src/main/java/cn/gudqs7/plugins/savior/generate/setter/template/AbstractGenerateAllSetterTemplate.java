package cn.gudqs7.plugins.savior.generate.setter.template;

import cn.gudqs7.plugins.savior.generate.base.AbstractVariableGenerateTemplate;
import cn.gudqs7.plugins.savior.generate.base.BaseVar;
import cn.gudqs7.plugins.savior.generate.base.GenerateBase;
import cn.gudqs7.plugins.savior.generate.setter.GenerateSetter;
import org.jetbrains.annotations.NotNull;

/**
 * @author WQ
 * @date 2021/10/1
 */
public abstract class AbstractGenerateAllSetterTemplate extends AbstractVariableGenerateTemplate {

    private final boolean generateDefaultVal;

    public AbstractGenerateAllSetterTemplate(boolean generateDefaultVal, String templateName, String example) {
        super(templateName, example);
        this.generateDefaultVal = generateDefaultVal;
    }

    @NotNull
    @Override
    protected GenerateBase getGenerateByVar(BaseVar baseVar) {
        return new GenerateSetter(generateDefaultVal, baseVar);
    }
}
