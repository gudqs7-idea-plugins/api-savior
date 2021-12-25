package cn.gudqs7.plugins.generate.setter;

import cn.gudqs7.plugins.generate.base.AbstractMethodListGenerate;
import cn.gudqs7.plugins.generate.base.BaseVar;
import cn.gudqs7.plugins.generate.util.BaseTypeUtil;
import cn.gudqs7.plugins.util.PsiUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.PsiClassReferenceType;

import java.util.Set;

/**
 * @author WQ
 * @date 2021/10/7
 */
public abstract class AbstractDefaultValGenerate extends AbstractMethodListGenerate {

    private final boolean generateDefaultVal;

    public AbstractDefaultValGenerate(boolean generateDefaultVal, BaseVar baseVar) {
        super(baseVar);
        this.generateDefaultVal = generateDefaultVal;
    }

    protected String generateDefaultVal(Project project, Set<String> newImportList, PsiParameter parameter) {
        String defaultVal = "";
        if (generateDefaultVal) {
            PsiType psiType = parameter.getType();
            String typeName = psiType.getPresentableText();
            String defaultVal0 = BaseTypeUtil.getJavaBaseTypeDefaultVal(typeName);
            // 基础类型
            if (defaultVal0 != null) {
                defaultVal = defaultVal0;
            } else {
                if (psiType instanceof PsiClassReferenceType) {
                    PsiClassReferenceType psiClassReferenceType = (PsiClassReferenceType) psiType;
                    PsiClass psiClass = psiClassReferenceType.resolve();
                    if (psiClass == null) {
                        PsiUtil.handleSyntaxError(psiClassReferenceType.getCanonicalText());
                    }
                    String typeName0 = psiClass.getQualifiedName();
                    String commonDefaultVal = BaseTypeUtil.getCommonDefaultVal(typeName0);
                    if (commonDefaultVal != null) {
                        defaultVal = commonDefaultVal;
                        String commonDefaultValImport = BaseTypeUtil.getCommonDefaultValImport(typeName0);
                        if (commonDefaultValImport != null) {
                            newImportList.add(commonDefaultValImport);
                        }
                    } else {
                        String fieldType = handlerByPsiType(newImportList, project, psiType, "%s", typeName);
                        defaultVal = "new " + fieldType + "()";
                    }
                }
            }
        }
        return defaultVal;
    }

}
