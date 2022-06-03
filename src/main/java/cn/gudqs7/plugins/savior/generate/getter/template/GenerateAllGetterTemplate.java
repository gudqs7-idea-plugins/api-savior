package cn.gudqs7.plugins.savior.generate.getter.template;

import cn.gudqs7.plugins.savior.generate.base.AbstractVariableGenerateTemplate;
import cn.gudqs7.plugins.savior.generate.base.BaseVar;
import cn.gudqs7.plugins.savior.generate.getter.GenerateGetter;
import org.jetbrains.annotations.NotNull;

/**
 * @author WQ
 * @date 2021/10/1
 */
public class GenerateAllGetterTemplate extends AbstractVariableGenerateTemplate {

    public GenerateAllGetterTemplate() {
        super("allget", "Generate Getter");
    }

    @Override
    protected @NotNull GenerateGetter getGenerateByVar(BaseVar baseVar) {
        return new GenerateGetter(baseVar);
    }
}
