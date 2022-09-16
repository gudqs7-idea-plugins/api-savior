package cn.gudqs7.plugins.generate.convert.template;

import cn.gudqs7.plugins.common.util.structure.PsiExpressionUtil;
import cn.gudqs7.plugins.generate.base.BaseGenerate;
import cn.gudqs7.plugins.generate.base.BaseGenerateTemplate;
import cn.gudqs7.plugins.generate.base.BaseVar;
import cn.gudqs7.plugins.generate.convert.ConvertForDstGenerate;
import cn.gudqs7.plugins.generate.convert.ConvertGenerate;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;

/**
 * @author WQ
 */
public class ConvertGenerateTemplate extends BaseGenerateTemplate {

    public ConvertGenerateTemplate() {
        super("convert", "Generate Convert by to.setXxx(from.getXxx)", ConvertGenerateTemplate::isApplicable0);
    }

    private static boolean isApplicable0(PsiElement psiElement) {
        if (psiElement == null) {
            return false;
        }
        if (psiElement instanceof PsiIdentifier) {
            PsiElement parent = psiElement.getParent();
            if (parent instanceof PsiReferenceExpression) {
                PsiReferenceExpression expression = (PsiReferenceExpression) parent;
                if (expression.getType() != null) {
                    return true;
                }
            }
        }
        if (psiElement instanceof PsiExpression) {
            PsiExpression psiExpression = (PsiExpression) psiElement;
            PsiType psiType = psiExpression.getType();
            return psiType != null;
        }
        if (psiElement instanceof PsiJavaToken) {
            PsiJavaToken psiJavaToken = (PsiJavaToken) psiElement;
            IElementType tokenType = psiJavaToken.getTokenType();
            String tokenTypeName = tokenType.toString();
            switch (tokenTypeName) {
                case "SEMICOLON":
                    // 即分号 ;
                    psiElement = psiElement.getPrevSibling();
                    break;
                case "RPARENTH":
                    // 即右括号 )
                    PsiElement methodParamExpression = psiElement.getParent();
                    if (methodParamExpression != null) {
                        psiElement = methodParamExpression.getParent();
                    }
                    break;
                default:
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
    protected BaseGenerate buildGenerate(PsiElement psiElement, PsiFile containingFile, PsiDocumentManager psiDocumentManager, Document document) {
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
            return new ConvertGenerate(varForSet, varForGet);
        }
        if (psiElement instanceof PsiExpression) {
            PsiExpression psiExpression = (PsiExpression) psiElement;
            PsiType psiType = psiExpression.getType();
            String varName = psiExpression.getText();
            if (psiType != null) {
                BaseVar baseVar = new BaseVar();
                baseVar.setVarName(varName);
                baseVar.setVarType(psiType);
                return new ConvertForDstGenerate(baseVar, null);
            }
        }
        return null;
    }


}