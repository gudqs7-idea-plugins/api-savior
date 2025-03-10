package cn.gudqs7.plugins.common.util.structure;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.psi.*;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * 注解工具类
 *
 * @author wq
 * @date 2022/5/29
 */
public class PsiAnnotationUtil {

    /**
     * 获取数组类型的注解字段数据
     *
     * @param fieldAnnotation 注解本体
     * @param attr            字段名称
     * @param <T>             数组元素类型
     * @return 数组类型的注解字段数据
     */
    public static <T> List<T> getAnnotationListValue(PsiAnnotation fieldAnnotation, String attr) {
        return getAnnotationListValue(fieldAnnotation, attr, null);
    }

    /**
     * 获取注解字段数据
     *
     * @param fieldAnnotation 注解本体
     * @param attr            字段名称
     * @param defaultVal      默认值
     * @param <T>             注解字段类型
     * @return 注解字段数据
     */
    public static <T> T getAnnotationValue(PsiAnnotation fieldAnnotation, String attr, T defaultVal) {
        PsiAnnotationMemberValue memberValue = fieldAnnotation.findAttributeValue(attr);
        if (memberValue == null) {
            return defaultVal;
        }
        Object value = getValueByPsiAnnotationMemberValue(memberValue);
        if (value != null) {
            return (T) value;
        }
        return defaultVal;
    }

    /**
     * 获取注解字段数据
     *
     * @param fieldAnnotation 注解本体
     * @param attr            字段名称
     * @param <T>             注解字段类型
     * @return 注解字段数据
     */
    public static <T> T getAnnotationValue(PsiAnnotation fieldAnnotation, String attr) {
        return getAnnotationValue(fieldAnnotation, attr, null);
    }

    /**
     * 获取数组类型的注解字段数据
     *
     * @param fieldAnnotation 注解本体
     * @param attr            字段名称
     * @param <T>             数组元素类型
     * @param defaultVal      默认值
     * @return 数组类型的注解字段数据
     */
    public static <T> List<T> getAnnotationListValue(PsiAnnotation fieldAnnotation, String attr, List<T> defaultVal) {
        List<T> valList = new ArrayList<>();
        PsiAnnotationMemberValue memberValue = fieldAnnotation.findAttributeValue(attr);
        if (memberValue == null) {
            return defaultVal;
        }
        List<PsiAnnotationMemberValue> psiAnnotationMemberValueList = AnnotationUtil.arrayAttributeValues(memberValue);
        for (PsiAnnotationMemberValue psiAnnotationMemberValue : psiAnnotationMemberValueList) {
            Object value = getValueByPsiAnnotationMemberValue(psiAnnotationMemberValue);
            if (value != null) {
                valList.add((T) value);
            }
        }
        return valList;
    }

    @NotNull
    @SneakyThrows
    public static <T> T getAnnotationInfoByPojo(PsiAnnotation psiAnnotation,@NotNull Class<T> clazz) {
        Constructor<T> constructor = clazz.getConstructor((Class<?>[]) null);
        T instance = constructor.newInstance((Object[]) null);
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers)) {
                continue;
            }
            String fieldName = field.getName();
            Class<?> fieldType = field.getType();

            // 数组类型
            if (List.class.isAssignableFrom(fieldType)) {
                List<Object> annotationListValue = getAnnotationListValue(psiAnnotation, fieldName, null);
                if (fieldType.isInstance(annotationListValue)) {
                    field.setAccessible(true);
                    field.set(instance, annotationListValue);
                }
            } else {
                Object annotationValue = getAnnotationValue(psiAnnotation, fieldName, null);
                if (fieldType.isInstance(annotationValue)) {
                    field.setAccessible(true);
                    field.set(instance, annotationValue);
                }
            }
        }
        return instance;
    }


    private static Object getValueByPsiAnnotationMemberValue(PsiAnnotationMemberValue value) {
        if (value instanceof PsiReferenceExpression) {
            PsiReferenceExpression psiReferenceExpression = (PsiReferenceExpression) value;
            PsiElement resolvedElement = psiReferenceExpression.resolve();
            // 如果解析到的是一个字段（如静态常量）
            if (resolvedElement instanceof PsiField) {
                PsiField field = (PsiField) resolvedElement;
                PsiExpression initializer = field.getInitializer();
                if (initializer != null) {
                    // 递归解析字段的初始化表达式
                    return getValueByPsiAnnotationMemberValue(initializer);
                }
            }
        } else {
            // 尝试直接计算常量表达式的值
            return computeConstantExpression(value);
        }
        return null;
    }

    private static Object computeConstantExpression(PsiAnnotationMemberValue psiAnnotationMemberValue) {
        return JavaPsiFacade.getInstance(psiAnnotationMemberValue.getProject()).getConstantEvaluationHelper().computeConstantExpression(psiAnnotationMemberValue);
    }

}
