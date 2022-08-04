package cn.gudqs7.plugins.generate.convert;

import cn.gudqs7.plugins.common.util.structure.PsiTypeUtil;
import cn.gudqs7.plugins.generate.base.BaseVar;
import com.intellij.psi.PsiType;

import java.util.HashSet;

/**
 * @author WQ
 * @date 2021/10/1
 */
public class GenerateConvertForListInner extends GenerateConvertForInner {

    public GenerateConvertForListInner(BaseVar varForSet, BaseVar varForGet, int innerLevel, String getterCode) {
        super(varForSet, varForGet, innerLevel, getterCode);
    }

    @Override
    protected String changeSplitText(String splitText) {
        if (innerLevel == 1) {
            return splitText;
        }
        // 累加即可
        return splitText + "        ";
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

        /*
        List<FooDTO> fooListDst = null;
        List<FooBO> fooListSrc = src.getFooList();
        if (fooListSrc != null) {
            fooListDst = fooListSrc.stream().map(fooListSrcItem -> {
                FooDTO fooListDstItem = new FooDTO();
                fooListDstItem.setStr1(fooListSrcItem.getStr1());
                fooListDstItem.setStr2(fooListSrcItem.getStr2());
                return fooListDstItem;
            }).collect(Collectors.toList());
        }
         */

        String varItemVarName = varName + "Item";
        String varForGetItemVarName = varForGetVarName + "Item";

        builder.append(splitText);
        builder.append("List<").append(dstClassName).append("> ").append(varName).append(" = null;").append(splitText);
        builder.append("List<").append(srcClassName).append("> ").append(varForGetVarName).append(" = ").append(getterCode).append(";").append(splitText);
        builder.append("if (").append(varForGetVarName).append(" != null) {").append(splitText);
        builder.append("    ").append(varName).append(" = ").append(varForGetVarName).append(".stream().filter(Objects::nonNull).map(").append(varForGetItemVarName).append(" -> {").append(splitText);
        builder.append("        ").append(dstClassName).append(" ").append(varItemVarName).append(" = new ").append(dstClassName).append("();").append(splitText);

        baseVar.setVarName(varItemVarName);
        varForGet.setVarName(varForGetItemVarName);
    }

    @Override
    protected void doAppend(StringBuilder builder, String codeByMethod, String splitText, HashSet<String> newImportList) {
        builder.append("        ").append(codeByMethod).append(splitText);
    }

    @Override
    protected void afterAppend(StringBuilder builder, String splitText, HashSet<String> newImportList) {
        if (baseVar == null) {
            return;
        }
        String varName = baseVar.getVarName();
        builder.append("        return ").append(varName).append(";").append(splitText);
        builder.append("    }).collect(Collectors.toList());");
        builder.append("}").append(splitText);
    }

}
