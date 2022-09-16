package cn.gudqs7.plugins.generate.getter.template;

import cn.gudqs7.plugins.generate.base.AbstractVariableGenerateTemplate;
import cn.gudqs7.plugins.generate.base.BaseVar;
import cn.gudqs7.plugins.generate.getter.GetterGenerate;
import org.jetbrains.annotations.NotNull;

/**
 * @author WQ
 * @date 2021/10/1
 */
@SuppressWarnings("PostfixTemplateDescriptionNotFound")
public class AllGetterNoSuperClassGenerateTemplate extends AbstractVariableGenerateTemplate {

    public AllGetterNoSuperClassGenerateTemplate() {
        super("allgetp", "Generate Getter but no super class");
    }

    @Override
    protected @NotNull GetterGenerate getGenerateByVar(BaseVar baseVar) {
        return new GetterGenerate(baseVar, true);
    }
}
