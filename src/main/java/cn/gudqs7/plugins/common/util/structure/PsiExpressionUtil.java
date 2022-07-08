package cn.gudqs7.plugins.common.util.structure;

import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiType;

/**
 * 方法表达式相关工具类
 *
 * @author WQ
 * @date 2021/10/6
 */
public class PsiExpressionUtil {

    /**
     * 获取方法调用者变量名
     *
     * @param callExpression 方法表达式
     * @return 方法调用者变量名
     */
    public static String getNameByMethodCallExpression(PsiMethodCallExpression callExpression) {
        PsiExpression qualifierExpression = callExpression.getMethodExpression().getQualifierExpression();
        if (qualifierExpression != null) {
            return qualifierExpression.getText();
        }
        return null;
    }

    /**
     * 获取方法参数中的方法表达式的调用者的变量名
     *
     * @param callExpression 方法表达式
     * @return 方法参数中的方法表达式的调用者的变量名
     */
    public static String getNameByFirstArgument(PsiMethodCallExpression callExpression) {
        PsiMethodCallExpression expressionByFirstArgument = getPsiMethodCallExpressionByFirstArgument(callExpression);
        if (expressionByFirstArgument != null) {
            return getNameByMethodCallExpression(expressionByFirstArgument);
        }
        return null;
    }

    /**
     * 获取方法调用者变量类型
     *
     * @param callExpression 方法表达式
     * @return 方法调用者变量名
     */
    public static PsiType getPsiTypeByMethodCallExpression(PsiMethodCallExpression callExpression) {
        PsiExpression qualifierExpression = callExpression.getMethodExpression().getQualifierExpression();
        if (qualifierExpression != null) {
            return qualifierExpression.getType();
        }
        return null;
    }

    /**
     * 获取方法参数中的方法表达式的调用者的变量类型
     *
     * @param callExpression 方法表达式
     * @return 方法参数中的方法表达式的调用者的变量类型
     */
    public static PsiType getPsiTypeByFirstArgument(PsiMethodCallExpression callExpression) {
        PsiMethodCallExpression expressionByFirstArgument = getPsiMethodCallExpressionByFirstArgument(callExpression);
        if (expressionByFirstArgument != null) {
            return getPsiTypeByMethodCallExpression(expressionByFirstArgument);
        }
        return null;
    }

    /**
     * 获取方法参数中的方法表达式
     *
     * @param callExpression 方法表达式
     * @return 方法参数中的方法表达式
     */
    private static PsiMethodCallExpression getPsiMethodCallExpressionByFirstArgument(PsiMethodCallExpression callExpression) {
        PsiExpression[] expressions = callExpression.getArgumentList().getExpressions();
        if (expressions.length > 0) {
            PsiExpression expression = expressions[0];
            if (expression instanceof PsiMethodCallExpression) {
                return (PsiMethodCallExpression) expression;
            }
        }
        return null;
    }

}
