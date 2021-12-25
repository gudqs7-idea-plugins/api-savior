package cn.gudqs7.plugins.util;

import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiType;

/**
 * @author WQ
 * @date 2021/10/6
 */
public class PsiExpressionUtil {

    public static String getNameByMethodCallExpression(PsiMethodCallExpression callExpression) {
        PsiExpression qualifierExpression = callExpression.getMethodExpression().getQualifierExpression();
        if (qualifierExpression != null) {
            return qualifierExpression.getText();
        }
        return null;
    }

    public static PsiType getPsiTypeByMethodCallExpression(PsiMethodCallExpression callExpression) {
        PsiExpression qualifierExpression = callExpression.getMethodExpression().getQualifierExpression();
        if (qualifierExpression != null) {
            return qualifierExpression.getType();
        }
        return null;
    }

    public static PsiMethodCallExpression getPsiMethodCallExpressionByFirstArgument(PsiMethodCallExpression callExpression) {
        PsiExpression[] expressions = callExpression.getArgumentList().getExpressions();
        if (expressions.length > 0) {
            PsiExpression expression = expressions[0];
            if (expression instanceof PsiMethodCallExpression) {
                return (PsiMethodCallExpression) expression;
            }
        }
        return null;
    }

    public static PsiType getPsiTypeByFirstArgument(PsiMethodCallExpression callExpression) {
        PsiMethodCallExpression expressionByFirstArgument = getPsiMethodCallExpressionByFirstArgument(callExpression);
        if (expressionByFirstArgument != null) {
            return getPsiTypeByMethodCallExpression(expressionByFirstArgument);
        }
        return null;
    }

    public static String getNameByFirstArgument(PsiMethodCallExpression callExpression) {
        PsiMethodCallExpression expressionByFirstArgument = getPsiMethodCallExpressionByFirstArgument(callExpression);
        if (expressionByFirstArgument != null) {
            return getNameByMethodCallExpression(expressionByFirstArgument);
        }
        return null;
    }
}
