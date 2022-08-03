package cn.gudqs7.plugins.generate.getter;

import cn.gudqs7.plugins.common.util.structure.BaseTypeUtil;
import cn.gudqs7.plugins.common.util.structure.PsiMethodUtil;
import cn.gudqs7.plugins.generate.base.AbstractMethodListGenerate;
import cn.gudqs7.plugins.generate.base.BaseVar;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/**
 * @author WQ
 * @date 2021/10/1
 */
public class GenerateGetter extends AbstractMethodListGenerate {

    private final boolean noSuperClass;

    public GenerateGetter(BaseVar baseVar) {
        this(baseVar, false);
    }

    public GenerateGetter(BaseVar baseVar, boolean noSuperClass) {
        super(baseVar);
        this.noSuperClass = noSuperClass;
    }

    @Override
    protected List<PsiMethod> getGenerateMethodListByClass(PsiClass psiClass) {
        return PsiMethodUtil.getGetterMethod(psiClass, noSuperClass);
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
        PsiType psiType = method.getReturnType();
        String sourceVarName = baseVar.getVarName();
        if (psiType == null) {
            return sourceVarName + "." + methodName + "();";
        }
        Project project = method.getProject();
        String typeName = psiType.getPresentableText();
        String qName = psiType.getCanonicalText();
        String defaultValStrByQname = BaseTypeUtil.getDefaultValStrByQname(qName);
        if (defaultValStrByQname != null) {
            String commonDefaultValImport = BaseTypeUtil.getDefaultValImportByQname(qName);
            if (commonDefaultValImport != null) {
                newImportList.add(qName);
            }
        }
        Pair<String, String> pair = handlerByPsiType(newImportList, project, psiType, "%s", typeName);
        String fieldType = pair.getLeft();
        return fieldType + " " + recvVarName + " = " + sourceVarName + "." + methodName + "();";
    }

}
