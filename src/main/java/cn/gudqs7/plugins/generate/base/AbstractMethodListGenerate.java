package cn.gudqs7.plugins.generate.base;

import cn.gudqs7.plugins.common.util.jetbrain.PsiTypeUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiTypesUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author WQ
 * @date 2021/10/6
 */
public abstract class AbstractMethodListGenerate implements GenerateBase {

    @Nullable
    protected final BaseVar baseVar;

    public AbstractMethodListGenerate(@Nullable BaseVar baseVar) {
        this.baseVar = baseVar;
    }

    @NotNull
    public List<PsiMethod> getGenerateMethodList() {
        if (baseVar == null) {
            return new ArrayList<>();
        }
        PsiType varType = baseVar.getVarType();
        PsiClass psiClass = PsiTypesUtil.getPsiClass(varType);
        if (varType instanceof PsiClassType) {
            PsiClassType referenceType = (PsiClassType) varType;
            PsiTypeUtil.resolvePsiClassParameter(referenceType);
        }
        return getGenerateMethodListByClass(psiClass);
    }

    /**
     * 根据 baseVar 对应的具体的 psiClass 获取其需要的方法列表
     * @param psiClass psiClass
     * @return 其需要的方法列表
     */
    protected abstract List<PsiMethod> getGenerateMethodListByClass(PsiClass psiClass);

    @Override
    public String generateCode(String splitText, HashSet<String> newImportList) {
        List<PsiMethod> methodList = getGenerateMethodList();
        if (methodList.size() == 0) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        beforeAppend(builder, splitText, newImportList);
        for (PsiMethod method : methodList) {
            String codeByMethod = generateCodeByMethod(newImportList, method);
            doAppend(builder, codeByMethod, splitText, newImportList);
        }
        afterAppend(builder, splitText, newImportList);
        return builder.toString();
    }

    protected void beforeAppend(StringBuilder builder, String splitText, HashSet<String> newImportList) {
        builder.append(splitText);
    }

    protected void doAppend(StringBuilder builder, String codeByMethod, String splitText, HashSet<String> newImportList) {
        builder.append(codeByMethod).append(splitText);
    }

    protected void afterAppend(StringBuilder builder, String splitText, HashSet<String> newImportList) {

    }

}
