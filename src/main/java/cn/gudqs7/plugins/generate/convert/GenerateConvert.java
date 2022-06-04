package cn.gudqs7.plugins.generate.convert;

import cn.gudqs7.plugins.common.util.structure.PsiMethodUtil;
import cn.gudqs7.plugins.generate.base.AbstractMethodListGenerate;
import cn.gudqs7.plugins.generate.base.BaseVar;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.util.PsiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author WQ
 * @date 2021/10/1
 */
public class GenerateConvert extends AbstractMethodListGenerate {

    protected final BaseVar varForGet;

    public GenerateConvert(BaseVar varForSet, BaseVar varForGet) {
        super(varForSet);
        this.varForGet = varForGet;
    }

    @Override
    protected List<PsiMethod> getGenerateMethodListByClass(PsiClass psiClass) {
        return PsiMethodUtil.getSetterMethod(psiClass);
    }

    @Override
    @NotNull
    public String generateCodeByMethod(Set<String> newImportList, PsiMethod method) {
        if (baseVar == null || varForGet == null) {
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
            String getMethodName = methodName.replaceFirst("set", methodPrefix);
            String setVal = getSetVal(getMethodName);
            return generateName + "." + methodName + "(" + setVal + ");";
        } else {
            return "";
        }
    }

    @NotNull
    private String getSetVal(String getMethodName) {
        PsiClass psiClassForGet = PsiTypesUtil.getPsiClass(varForGet.getVarType());
        List<PsiMethod> methodList = PsiMethodUtil.getGetterMethod(psiClassForGet);
        Map<String, PsiMethod> methodMap = PsiMethodUtil.convertMethodListToMap(methodList);
        PsiMethod psiMethod = methodMap.get(getMethodName);
        String defaultVal = "null/* 源对象无此字段 */";
        if (psiMethod != null) {
            defaultVal = getGetterCode(psiMethod);
        }
        return defaultVal;
    }

    @NotNull
    private String getGetterCode(PsiMethod method) {
        String methodName = method.getName();
        return varForGet.getVarName() + "." + methodName + "()";
    }

}
