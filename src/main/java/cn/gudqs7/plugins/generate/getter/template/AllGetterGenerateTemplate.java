package cn.gudqs7.plugins.generate.getter.template;

import cn.gudqs7.plugins.generate.base.AbstractVariableGenerateTemplate;
import cn.gudqs7.plugins.generate.base.BaseVar;
import cn.gudqs7.plugins.generate.getter.GetterGenerate;
import org.jetbrains.annotations.NotNull;

/**
 * @author WQ
 * @date 2021/10/1
 */
public class AllGetterGenerateTemplate extends AbstractVariableGenerateTemplate {

    public AllGetterGenerateTemplate() {
        super("allget", "Generate Getter");
    }

    @Override
    protected @NotNull GetterGenerate getGenerateByVar(BaseVar baseVar) {
        return new GetterGenerate(baseVar);
    }
}
