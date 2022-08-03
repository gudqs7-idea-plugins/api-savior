package cn.gudqs7.plugins.generate.setter;

import cn.gudqs7.plugins.common.util.structure.PsiMethodUtil;
import cn.gudqs7.plugins.generate.base.BaseVar;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author WQ
 * @date 2021/10/1
 */
public class GenerateChain extends AbstractDefaultValGenerate {

    public GenerateChain(boolean generateDefaultVal, BaseVar baseVar) {
        super(generateDefaultVal, baseVar);
    }

    @Override
    @NotNull
    public List<PsiMethod> getGenerateMethodListByClass(PsiClass psiClass) {
        return PsiMethodUtil.getSetterMethod(psiClass, false);
    }

    @Override
    @NotNull
    public String generateCodeByMethod(Set<String> newImportList, PsiMethod method) {
        if (baseVar == null) {
            return "";
        }
        Project project = method.getProject();
        PsiParameterList parameterList = method.getParameterList();
        PsiParameter[] parameters = parameterList.getParameters();
        if (parameters.length > 0) {
            PsiParameter parameter = parameters[0];
            String methodName = method.getName();
            // todo get annotation of @Accessors.fluent is true or false, then change methodName to adapt it
            String defaultVal = generateDefaultVal(project, newImportList, parameter);
            return "." + methodName + "(" + defaultVal + ")";
        } else {
            return "";
        }
    }

    @Override
    protected void beforeAppend(StringBuilder builder, String splitText, HashSet<String> newImportList) {
        if (baseVar == null) {
            return;
        }
        String typeFullName = baseVar.getVarName();
        String varName = typeFullName.substring(0, 1).toLowerCase() + typeFullName.substring(1);
        builder.append(varName);
    }

    @Override
    protected void doAppend(StringBuilder builder, String codeByMethod, String splitText, HashSet<String> newImportList) {
        builder.append(codeByMethod).append("\n");
    }

    @Override
    protected void afterAppend(StringBuilder builder, String splitText, HashSet<String> newImportList) {
        builder.deleteCharAt(builder.length() - 1);
        builder.append(";");
    }
}
