package cn.gudqs7.plugins.common.util.structure;

import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * PsiMethod 相关工具类
 *
 * @author wq
 * @date 2022/6/4
 */
public class PsiMethodUtil {

    private static final String GET_METHOD_PREFIX = "get";
    private static final String IS_METHOD_PREFIX = "is";
    private static final String SET_METHOD_PREFIX = "set";

    /**
     * 判断类是否有 getter
     *
     * @param psiClass 类
     * @return 类是否有 getter
     */
    public static boolean checkClassHasValidGetter(PsiClass psiClass) {
        return checkClassHasValidMethod(psiClass, GET_METHOD_PREFIX, IS_METHOD_PREFIX);
    }

    /**
     * 判断类是否有 setter
     *
     * @param psiClass 类
     * @return 类是否有 setter
     */
    public static boolean checkClassHasValidSetter(PsiClass psiClass) {
        return checkClassHasValidMethod(psiClass, SET_METHOD_PREFIX);
    }

    /**
     * 返回类的所有 getter
     *
     * @param psiClass 类
     * @return 类的所有 getter
     */
    @NotNull
    public static List<PsiMethod> getGetterMethod(PsiClass psiClass) {
        return getMethodByPrefix(psiClass, GET_METHOD_PREFIX, IS_METHOD_PREFIX);
    }

    /**
     * 返回类的所有 setter
     *
     * @param psiClass 类
     * @return 类的所有 setter
     */
    @NotNull
    public static List<PsiMethod> getSetterMethod(PsiClass psiClass) {
        return getMethodByPrefix(psiClass, SET_METHOD_PREFIX);
    }

    /**
     * 返回键值对, 键为方法名称
     *
     * @param methodList 方法集合
     * @return 方法键值对
     */
    public static Map<String, PsiMethod> convertMethodListToMap(@NotNull List<PsiMethod> methodList) {
        return methodList.stream().collect(Collectors.toMap(PsiMethod::getName, Function.identity(), (m1, m2) -> m1));
    }

    /**
     * 判断这个 setter 对应的字段是否为 boolean 类型(不包括 Boxing)
     *
     * @param method 方法
     * @return setter 对应的字段是否为 boolean 类型
     */
    public static boolean setterIsBoolType(@NotNull PsiMethod method) {
        PsiParameter[] parameters = method.getParameterList().getParameters();
        if (parameters.length > 0) {
            PsiParameter parameter = parameters[0];
            PsiType psiType = parameter.getType();
            if (!(psiType instanceof PsiClassReferenceType)) {
                return "boolean".equals(psiType.getCanonicalText());
            }
        }
        return false;
    }

    /**
     * 判断类是否有指定前缀的方法
     *
     * @param psiClass    类
     * @param prefixArray 指定前缀数组
     * @return 类是否有指定前缀的方法
     */
    private static boolean checkClassHasValidMethod(PsiClass psiClass, String... prefixArray) {
        if (psiClass == null) {
            return false;
        }
        while (PsiClassUtil.isNotSystemClass(psiClass)) {
            for (PsiMethod method : psiClass.getMethods()) {
                if (isValidMethod(method, prefixArray)) {
                    return true;
                }
            }
            psiClass = psiClass.getSuperClass();
        }
        return false;
    }

    /**
     * 校验方法是否为 public, 且不为 static, 且为方法名符合指定前缀
     *
     * @param method      方法
     * @param prefixArray 指定前缀
     * @return 方法是否为 public, 且不为 static, 且为方法名符合指定前缀
     */
    private static boolean isValidMethod(@NotNull PsiMethod method, String... prefixArray) {
        if (method.hasModifierProperty(PsiModifier.PUBLIC) && !method.hasModifierProperty(PsiModifier.STATIC)) {
            if (prefixArray != null && prefixArray.length > 0) {
                boolean flag = false;
                String methodName = method.getName();
                for (String prefix : prefixArray) {
                    flag = flag || methodName.startsWith(prefix);
                }
                return flag;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * 返回类的指定前缀的方法
     *
     * @param psiClass    类
     * @param prefixArray 指定前缀集合
     * @return 方法列表
     */
    @NotNull
    private static List<PsiMethod> getMethodByPrefix(PsiClass psiClass, String... prefixArray) {
        List<PsiMethod> methodList = new ArrayList<>();
        Set<String> methodNameSet = new HashSet<>();
        while (PsiClassUtil.isNotSystemClass(psiClass)) {
            addMethodToList(methodList, methodNameSet, psiClass, prefixArray);
            psiClass = psiClass.getSuperClass();
        }
        return methodList;
    }

    /**
     * 往方法集合添加类的符合指定前缀的方法(去重, 但有序)
     *
     * @param methodList    方法集合
     * @param methodNameSet 方法名称 set
     * @param psiClass      类
     * @param prefixArray   指定前缀
     */
    private static void addMethodToList(List<PsiMethod> methodList, Set<String> methodNameSet, @NotNull PsiClass psiClass, String... prefixArray) {
        PsiMethod[] methods = psiClass.getMethods();
        for (PsiMethod method : methods) {
            if (isValidMethod(method, prefixArray)) {
                String methodName = method.getName();
                if (methodNameSet.contains(methodName)) {
                    continue;
                }
                methodNameSet.add(methodName);
                methodList.add(method);
            }
        }
    }
}
