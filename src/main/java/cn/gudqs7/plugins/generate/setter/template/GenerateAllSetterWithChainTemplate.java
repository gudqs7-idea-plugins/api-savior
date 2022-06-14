package cn.gudqs7.plugins.generate.setter.template;

import cn.gudqs7.plugins.generate.base.AbstractVariableGenerateTemplate;
import cn.gudqs7.plugins.generate.base.BaseVar;
import cn.gudqs7.plugins.generate.base.GenerateBase;
import cn.gudqs7.plugins.generate.setter.GenerateChain;
import org.jetbrains.annotations.NotNull;

/**
 * @author WQ
 * @date 2021/10/1
 */
public class GenerateAllSetterWithChainTemplate extends AbstractVariableGenerateTemplate {

    private final boolean generateDefaultVal;

    public GenerateAllSetterWithChainTemplate() {
        super("allsetc", "Generate setter with chain; if you using @Accessors(chain = true)");
        this.generateDefaultVal = true;
    }

    @NotNull
    @Override
    protected GenerateBase getGenerateByVar(BaseVar baseVar) {
        return new GenerateChain(generateDefaultVal, baseVar);
    }
}
