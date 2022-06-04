package cn.gudqs7.plugins.generate.convert.template;

import cn.gudqs7.plugins.common.util.jetbrain.PsiExpressionUtil;
import cn.gudqs7.plugins.generate.base.BaseVar;
import cn.gudqs7.plugins.generate.base.GenerateBase;
import cn.gudqs7.plugins.generate.base.GenerateBaseTemplate;
import cn.gudqs7.plugins.generate.convert.GenerateConvert;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;

/**
 * @author WQ
 */
public class GenerateConvertTemplate extends GenerateBaseTemplate {

    public GenerateConvertTemplate() {
        super("convert", "Generate Convert by to.setXxx(from.getXxx)", GenerateConvertTemplate::isApplicable0);
    }

    private static boolean isApplicable0(PsiElement psiElement) {
        if (psiElement == null) {
            return false;
        }
        if (psiElement instanceof PsiJavaToken) {
            PsiJavaToken psiJavaToken = (PsiJavaToken) psiElement;
            IElementType tokenType = psiJavaToken.getTokenType();
            String tokenTypeName = tokenType.toString();
            switch (tokenTypeName) {
                case "SEMICOLON":
                    psiElement = psiElement.getPrevSibling();
                    break;
                case "RPARENTH":
                    psiElement = psiElement.getParent().getParent();
                    break;
                default:
                    psiElement = psiElement.getParent().getParent();
                    break;
            }
        }
        if (psiElement instanceof PsiMethodCallExpression) {
            PsiMethodCallExpression callExpression = (PsiMethodCallExpression) psiElement;
            PsiType psiTypeForSet = PsiExpressionUtil.getPsiTypeByMethodCallExpression(callExpression);
            PsiType psiTypeForGet = PsiExpressionUtil.getPsiTypeByFirstArgument(callExpression);
            return psiTypeForSet != null && psiTypeForGet != null;
        }
        return false;
    }

    @Override
    protected GenerateBase buildGenerate(PsiElement psiElement, PsiFile containingFile, PsiDocumentManager psiDocumentManager, Document document) {
        if (psiElement instanceof PsiMethodCallExpression) {
            PsiMethodCallExpression callExpression = (PsiMethodCallExpression) psiElement;
            PsiType psiTypeForSet = PsiExpressionUtil.getPsiTypeByMethodCallExpression(callExpression);
            String nameForSet = PsiExpressionUtil.getNameByMethodCallExpression(callExpression);
            if (psiTypeForSet == null) {
                return null;
            }
            PsiType psiTypeForGet = PsiExpressionUtil.getPsiTypeByFirstArgument(callExpression);
            String nameForGet = PsiExpressionUtil.getNameByFirstArgument(callExpression);
            if (psiTypeForGet == null) {
                return null;
            }
            BaseVar varForSet = new BaseVar();
            varForSet.setVarName(nameForSet);
            varForSet.setVarType(psiTypeForSet);

            BaseVar varForGet = new BaseVar();
            varForGet.setVarName(nameForGet);
            varForGet.setVarType(psiTypeForGet);
            return new GenerateConvert(varForSet, varForGet);
        }
        return null;
    }


}