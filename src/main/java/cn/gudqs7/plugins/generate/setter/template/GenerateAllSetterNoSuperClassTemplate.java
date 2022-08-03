package cn.gudqs7.plugins.generate.setter.template;

import cn.gudqs7.plugins.generate.base.AbstractVariableGenerateTemplate;
import cn.gudqs7.plugins.generate.base.BaseVar;
import cn.gudqs7.plugins.generate.base.GenerateBase;
import cn.gudqs7.plugins.generate.setter.GenerateSetter;
import org.jetbrains.annotations.NotNull;

/**
 * @author WQ
 * @date 2021/10/1
 */
@SuppressWarnings("PostfixTemplateDescriptionNotFound")
public class GenerateAllSetterNoSuperClassTemplate extends AbstractVariableGenerateTemplate {

    public GenerateAllSetterNoSuperClassTemplate() {
        super("allsetp", "Generate Setter but no super class");
    }

    @NotNull
    @Override
    protected GenerateBase getGenerateByVar(BaseVar baseVar) {
        return new GenerateSetter(true, baseVar, true);
    }
}
