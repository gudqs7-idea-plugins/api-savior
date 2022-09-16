package cn.gudqs7.plugins.generate.convert;

import cn.gudqs7.plugins.common.util.structure.BaseTypeUtil;
import cn.gudqs7.plugins.common.util.structure.PsiMethodUtil;
import cn.gudqs7.plugins.common.util.structure.PsiTypeUtil;
import cn.gudqs7.plugins.generate.base.AbstractMethodListGenerate;
import cn.gudqs7.plugins.generate.base.BaseVar;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTypesUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author WQ
 * @date 2021/10/1
 */
public class ConvertGenerate extends AbstractMethodListGenerate {

    protected final BaseVar varForGet;

    public ConvertGenerate(BaseVar varForSet, BaseVar varForGet) {
        super(varForSet);
        this.varForGet = varForGet;
    }

    @Override
    protected List<PsiMethod> getGenerateMethodListByClass(PsiClass psiClass) {
        return PsiMethodUtil.getSetterMethod(psiClass, false);
    }

    @Override
    @NotNull
    public String generateCodeByMethod(PsiMethod method, String splitText, Set<String> newImportList) {
        if (baseVar == null) {
            return "";
        }
        String generateName = baseVar.getVarName();
        PsiParameterList parameterList = method.getParameterList();
        PsiParameter[] parameters = parameterList.getParameters();
        if (parameters.length > 0) {
            String methodName = method.getName();
            String methodPrefix = "get";
            boolean setterIsBoolean = PsiMethodUtil.setterIsBoolType(method);
            if (setterIsBoolean) {
                methodPrefix = "is";
            }
            String getterMethodName = methodName.replaceFirst("set", methodPrefix);
            Pair<String, String> dstValPair = getDstVal(getterMethodName, parameters[0], newImportList, splitText);
            String setVal = dstValPair.getLeft();
            String moreCode = dstValPair.getRight();
            return moreCode + generateName + "." + methodName + "(" + setVal + ");";
        } else {
            return "";
        }
    }

    @NotNull
    protected Pair<String, String> getDstVal(String getterMethodName, PsiParameter psiParameter, Set<String> newImportList, String splitText) {
        PsiClass psiClassForGet = PsiTypesUtil.getPsiClass(varForGet.getVarType());
        List<PsiMethod> methodList = PsiMethodUtil.getGetterMethod(psiClassForGet, false);
        Map<String, PsiMethod> methodMap = PsiMethodUtil.convertMethodListToMap(methodList);
        PsiMethod psiMethod = methodMap.get(getterMethodName);

        // 若源对象无此字段(即无相应的 getter), 则仍保留此 set 语句, 但 set 的值为 null
        // 主要是考虑到目标对象是我们关注的, 理论上目标对象的所有字段都应该是有用的, 因此要保留!
        String defaultVal = "null";
        String moreCode = "";
        if (psiMethod != null) {
            PsiType srcVarType = psiMethod.getReturnType();
            if (srcVarType != null) {
                String dstVarName = psiParameter.getName();
                PsiType dstVarType = psiParameter.getType();
                boolean samePsiType = PsiTypeUtil.isSamePsiType(srcVarType, dstVarType);

                Project project = psiMethod.getProject();
                boolean srcVarTypeFromList = PsiTypeUtil.isPsiTypeFromList(srcVarType, project);
                boolean dstVarTypeFromList = PsiTypeUtil.isPsiTypeFromList(dstVarType, project);
                PsiType srcElementType = PsiTypeUtil.getElementTypeFromList(srcVarType, project);
                PsiType dstElementType = PsiTypeUtil.getElementTypeFromList(dstVarType, project);
                boolean sameElementType = srcVarTypeFromList && dstVarTypeFromList && PsiTypeUtil.isSamePsiType(srcElementType, dstElementType);
                if (samePsiType || sameElementType || BaseTypeUtil.isBaseTypeOrObject(dstVarType)) {
                    // 若源对象无此字段(即无相应的 getter), 则仍保留此 set 语句, 但 set 的值为 null
                    // 主要是考虑到目标对象是我们关注的, 理论上目标对象的所有字段都应该是有用的, 因此要保留!
                    defaultVal = getGetterCode(psiMethod);
                } else {
                    // 发生了相同字段但不同类型的数据, 需再写一个 convert 语句
                    int innerLevel = getInnerLevel();
                    // 判断循环次数, 避免死循环, 当前仅保留 3 层. 即 A 开始算起, A -> B 算一层, B -> C 算一层 C -> D 算一层
                    if (innerLevel <= 3) {
                        BaseVar varForSetInner = new BaseVar();
                        String dstClassVarName = dstVarName + "Dst" + innerLevel;
                        varForSetInner.setVarName(dstClassVarName);
                        varForSetInner.setVarType(dstVarType);
                        if (dstVarTypeFromList) {
                            varForSetInner.setVarType(dstElementType);
                        }
                        BaseVar varForGetInner = new BaseVar();
                        varForGetInner.setVarName(dstVarName + "Src" + innerLevel);
                        varForGetInner.setVarType(srcVarType);
                        if (dstVarTypeFromList) {
                            varForGetInner.setVarType(srcElementType);
                        }

                        newImportList.add(varForGetInner.getVarType().getCanonicalText());
                        newImportList.add(varForSetInner.getVarType().getCanonicalText());

                        String getterCode = getGetterCode(psiMethod);

                        // 若为 List 类型, 则生成的代码不一样
                        ConvertGenerate generateConvertInner;
                        if (dstVarTypeFromList) {
                            generateConvertInner = new ConvertForListInnerGenerate(varForSetInner, varForGetInner, innerLevel + 1, getterCode);
                        } else {
                            generateConvertInner = new ConvertForInnerGenerate(varForSetInner, varForGetInner, innerLevel + 1, getterCode);
                        }
                        HashSet<String> newImportListInner = new HashSet<>();
                        moreCode = generateConvertInner.generateCode(splitText, newImportListInner);
                        newImportList.addAll(newImportListInner);
                        defaultVal = dstClassVarName;
                    }
                }
            }
        }
        return Pair.of(defaultVal, moreCode);
    }

    protected int getInnerLevel() {
        return 0;
    }

    @NotNull
    private String getGetterCode(PsiMethod method) {
        String methodName = method.getName();
        return varForGet.getVarName() + "." + methodName + "()";
    }

}
