package cn.gudqs7.plugins.generate.convert;

import cn.gudqs7.plugins.common.util.structure.PsiTypeUtil;
import cn.gudqs7.plugins.generate.base.BaseVar;
import com.intellij.psi.PsiType;

import java.util.HashSet;

/**
 * @author WQ
 * @date 2021/10/1
 */
public class GenerateConvertForInner extends GenerateConvert {

    protected final int innerLevel;
    protected final String getterCode;

    public GenerateConvertForInner(BaseVar varForSet, BaseVar varForGet, int innerLevel, String getterCode) {
        super(varForSet, varForGet);
        this.innerLevel = innerLevel;
        this.getterCode = getterCode;
    }

    @Override
    protected String changeSplitText(String splitText) {
        if (innerLevel == 1) {
            return splitText;
        }
        // 累加即可
        return splitText + "    ";
    }

    @Override
    protected void beforeAppend(StringBuilder builder, String splitText, HashSet<String> newImportList) {
        if (baseVar == null || varForGet == null) {
            return;
        }
        String varName = baseVar.getVarName();
        PsiType psiType = baseVar.getVarType();
        String varForGetVarName = varForGet.getVarName();
        PsiType varForGetVarType = varForGet.getVarType();
        String dstClassName = PsiTypeUtil.getClassName(psiType, null);
        if (dstClassName == null) {
            dstClassName = psiType.getPresentableText();
        }
        String srcClassName = PsiTypeUtil.getClassName(varForGetVarType, null);
        if (srcClassName == null) {
            srcClassName = varForGetVarType.getPresentableText();
        }

        builder.append(splitText);
        builder.append(dstClassName).append(" ").append(varName).append(" = null;").append(splitText);
        builder.append(srcClassName).append(" ").append(varForGetVarName).append(" = ").append(getterCode).append(";").append(splitText);
        builder.append("if (").append(varForGetVarName).append(" != null) {").append(splitText);
        builder.append("    ").append(varName).append(" = new ").append(dstClassName).append("();").append(splitText);
    }

    @Override
    protected void doAppend(StringBuilder builder, String codeByMethod, String splitText, HashSet<String> newImportList) {
        builder.append("    ").append(codeByMethod).append(splitText);
    }

    @Override
    protected void afterAppend(StringBuilder builder, String splitText, HashSet<String> newImportList) {
        builder.append("}").append(splitText);
    }

    @Override
    protected int getInnerLevel() {
        return innerLevel;
    }
}
