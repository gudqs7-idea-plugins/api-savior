package cn.gudqs7.plugins.generate.convert;

import cn.gudqs7.plugins.generate.base.BaseVar;
import com.intellij.codeInsight.template.impl.Variable;
import com.intellij.psi.PsiParameter;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * @author wenquan
 * @date 2022/8/3
 */
public class ConvertForDstGenerate extends ConvertGenerate {

    public ConvertForDstGenerate(BaseVar varForSet, BaseVar varForGet) {
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
    protected Pair<String, String> getDstVal(String getterMethodName, PsiParameter parameter, Set<String> newImportList, String splitText) {
        return Pair.of("$SOURCE$." + getterMethodName + "()", "");
    }

}
