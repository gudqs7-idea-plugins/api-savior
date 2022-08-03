package cn.gudqs7.plugins.generate.convert;

import cn.gudqs7.plugins.generate.base.BaseVar;
import com.intellij.codeInsight.template.impl.Variable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author wenquan
 * @date 2022/8/3
 */
public class GenerateConvertForDst extends GenerateConvert {

    public GenerateConvertForDst(BaseVar varForSet, BaseVar varForGet) {
        super(varForSet, varForGet);
    }

    @Nullable
    @Override
    public Variable[] getTemplateVariables() {
        Variable variable = new Variable("SOURCE", "camelCase(\"sourceObj\")", "sourceObj", true);
        return new Variable[]{variable};
    }

    @NotNull
    @Override
    protected String getSetVal(String getMethodName) {
        return "$SOURCE$." + getMethodName + "()";
    }

}
