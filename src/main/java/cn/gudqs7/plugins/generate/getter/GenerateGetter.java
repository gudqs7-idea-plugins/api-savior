package cn.gudqs7.plugins.generate.getter;

import cn.gudqs7.plugins.generate.base.AbstractMethodListGenerate;
import cn.gudqs7.plugins.generate.base.BaseVar;
import cn.gudqs7.plugins.util.PsiClassUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/**
 * @author WQ
 * @date 2021/10/1
 */
public class GenerateGetter extends AbstractMethodListGenerate {

    public GenerateGetter(BaseVar baseVar) {
        super(baseVar);
    }

    @Override
    protected List<PsiMethod> getGenerateMethodListByClass(PsiClass psiClass) {
        return PsiClassUtil.getGetterMethod(psiClass);
    }

    @Override
    @NotNull
    public String generateCodeByMethod(Set<String> newImportList, PsiMethod method) {
        if (baseVar == null) {
            return "";
        }
        String methodName = method.getName();
        String recvVarName = methodName.replaceFirst("get", "");
        recvVarName = recvVarName.substring(0, 1).toLowerCase() + recvVarName.substring(1);
        PsiType returnType = method.getReturnType();
        String sourceVarName = baseVar.getVarName();
        if (returnType == null) {
            return sourceVarName + "." + methodName + "();";
        }
        Project project = method.getProject();
        String typeName = returnType.getPresentableText();
        String fieldType = handlerByPsiType(newImportList, project, returnType, "%s", typeName);
        return fieldType + " " + recvVarName + " = " + sourceVarName + "." + methodName + "();";
    }

}
