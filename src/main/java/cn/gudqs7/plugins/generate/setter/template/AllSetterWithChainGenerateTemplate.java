package cn.gudqs7.plugins.generate.setter.template;

import cn.gudqs7.plugins.generate.base.AbstractVariableGenerateTemplate;
import cn.gudqs7.plugins.generate.base.BaseGenerate;
import cn.gudqs7.plugins.generate.base.BaseVar;
import cn.gudqs7.plugins.generate.setter.ChainGenerate;
import org.jetbrains.annotations.NotNull;

/**
 * @author WQ
 * @date 2021/10/1
 */
public class AllSetterWithChainGenerateTemplate extends AbstractVariableGenerateTemplate {

    private final boolean generateDefaultVal;

    public AllSetterWithChainGenerateTemplate() {
        super("allsetc", "Generate setter with chain; if you using @Accessors(chain = true)");
        this.generateDefaultVal = true;
    }

    @NotNull
    @Override
    protected BaseGenerate getGenerateByVar(BaseVar baseVar) {
        return new ChainGenerate(generateDefaultVal, baseVar);
    }
}
