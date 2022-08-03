package cn.gudqs7.plugins.generate.getter.template;

import cn.gudqs7.plugins.generate.base.AbstractVariableGenerateTemplate;
import cn.gudqs7.plugins.generate.base.BaseVar;
import cn.gudqs7.plugins.generate.getter.GenerateGetter;
import org.jetbrains.annotations.NotNull;

/**
 * @author WQ
 * @date 2021/10/1
 */
@SuppressWarnings("PostfixTemplateDescriptionNotFound")
public class GenerateAllGetterNoSuperClassTemplate extends AbstractVariableGenerateTemplate {

    public GenerateAllGetterNoSuperClassTemplate() {
        super("allgetp", "Generate Getter but no super class");
    }

    @Override
    protected @NotNull GenerateGetter getGenerateByVar(BaseVar baseVar) {
        return new GenerateGetter(baseVar, true);
    }
}
