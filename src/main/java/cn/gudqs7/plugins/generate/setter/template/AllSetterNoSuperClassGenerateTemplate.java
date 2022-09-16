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
@SuppressWarnings("PostfixTemplateDescriptionNotFound")
public class AllSetterNoSuperClassGenerateTemplate extends AbstractVariableGenerateTemplate {

    public AllSetterNoSuperClassGenerateTemplate() {
        super("allsetp", "Generate Setter but no super class");
    }

    @NotNull
    @Override
    protected BaseGenerate getGenerateByVar(BaseVar baseVar) {
        return new SetterGenerate(true, baseVar, true);
    }
}
