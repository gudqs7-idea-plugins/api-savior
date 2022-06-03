package cn.gudqs7.plugins.savior.util;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiArrayInitializerMemberValue;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.impl.compiled.ClsArrayInitializerMemberValueImpl;
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wq
 * @date 2022/5/29
 */
public class PsiAnnotationUtil {


    // =============== annotation util ===============

    public static <T> List<T> getAnnotationListValue(PsiAnnotation fieldAnnotation, String attr, T defaultVal) {
        Object annotationValueByQname = getAnnotationValue(fieldAnnotation, attr, defaultVal);
        if (annotationValueByQname == null) {
            return null;
        }
        if (annotationValueByQname instanceof List) {
            return (List<T>) annotationValueByQname;
        } else {
            List<T> list = new ArrayList<>();
            list.add((T) annotationValueByQname);
            return list;
        }
    }

    public static <T> T getAnnotationValue(PsiAnnotation fieldAnnotation, String attr, T defaultVal) {
        PsiAnnotationMemberValue value = fieldAnnotation.findAttributeValue(attr);
        if (value == null) {
            return defaultVal;
        }
        Object valueByPsiAnnotationMemberValue = getValueByPsiAnnotationMemberValue(value);
        if (valueByPsiAnnotationMemberValue != null) {
            return (T) valueByPsiAnnotationMemberValue;
        }
        return defaultVal;
    }

    public static <T> T getValueByPsiAnnotationMemberValue(PsiAnnotationMemberValue value) {
        if (value instanceof ClsArrayInitializerMemberValueImpl) {
            ClsArrayInitializerMemberValueImpl clsArrayInitializerMemberValue = (ClsArrayInitializerMemberValueImpl) value;
            return (T) new ArrayList<>();
        }
        if (value instanceof PsiLiteralExpressionImpl) {
            PsiLiteralExpressionImpl expression = (PsiLiteralExpressionImpl) value;
            Object expressionValue = expression.getValue();
            if (expressionValue != null) {
                return (T) expressionValue;
            }
        }
        if (value instanceof PsiReferenceExpression) {
            PsiReferenceExpression psiReferenceExpression = (PsiReferenceExpression) value;
            String text = psiReferenceExpression.getText();
            String prefixWithRequestMethod = "RequestMethod.";
            if (text.startsWith(prefixWithRequestMethod)) {
                return (T) text.substring(prefixWithRequestMethod.length());
            } else {
                return (T) text;
            }
        }
        if (value instanceof PsiArrayInitializerMemberValue) {
            PsiArrayInitializerMemberValue psiArrayInitializerMemberValue = (PsiArrayInitializerMemberValue) value;
            PsiAnnotationMemberValue[] memberValues = psiArrayInitializerMemberValue.getInitializers();
            if (memberValues.length > 0) {
                List<Object> list = new ArrayList<>();
                for (PsiAnnotationMemberValue memberValue : memberValues) {
                    Object item = getValueByPsiAnnotationMemberValue(memberValue);
                    if (item == null) {
                        list.add(memberValue);
                    } else {
                        list.add(item);
                    }
                }
                if (list.size() > 0) {
                    return (T) list;
                }
            }
        }
        return null;
    }

}
