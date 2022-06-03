package cn.gudqs7.plugins.generate.convert;

import cn.gudqs7.plugins.generate.base.BaseVar;
import com.intellij.psi.PsiType;

import java.util.HashSet;

/**
 * @author WQ
 * @date 2021/10/1
 */
public class GenerateConvertForMethod extends GenerateConvert {

    public GenerateConvertForMethod(BaseVar varForSet, BaseVar varForGet) {
        super(varForSet, varForGet);
    }

    @Override
    protected void beforeAppend(StringBuilder builder, String splitText, HashSet<String> newImportList) {
        if (baseVar == null) {
            return;
        }
        String varName = baseVar.getVarName();
        PsiType psiType = baseVar.getVarType();
        String typeName = psiType.getPresentableText();
//        if (splitText.startsWith("\n")) {
//            builder.append(splitText.substring(1));
//        }
        builder.append(splitText);
        builder.append(typeName).append(" ").append(varName).append(" = ").append("new ").append(typeName).append("();").append(splitText);
    }

    @Override
    protected void afterAppend(StringBuilder builder, String splitText, HashSet<String> newImportList) {
        if (baseVar == null) {
            return;
        }
        String varName = baseVar.getVarName();
        builder.append("return ").append(varName).append(";");
    }
}
