package cn.gudqs7.plugins.util;

import cn.gudqs7.plugins.generate.consant.GenerateConst;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.gudqs7.plugins.util.PsiUtil.handleSyntaxError;

/**
 * @author wenquan
 * @date 2021/9/30
 */
public class PsiClassUtil {


    // =================     PsiClass相关工具     ====================

    public static PsiClassReferenceType getPsiClassByPsiType(PsiType psiType) {
        if (psiType != null) {
            if (psiType instanceof PsiClassReferenceType) {
                return (PsiClassReferenceType) psiType;
            }
        }
        return null;
    }

    @NotNull
    public static PsiMethod[] getAllMethods(PsiClass psiClass) {
        PsiMethod[] methods = psiClass.getMethods();
        PsiClassType[] extendsListTypes = psiClass.getExtendsListTypes();
        if (extendsListTypes.length == 0) {
            return methods;
        }

        List<PsiMethod> allMethods = new ArrayList<>();
        for (PsiClassType extendsListType : extendsListTypes) {
            PsiClass extendCls = extendsListType.resolve();
            if (extendCls == null) {
                handleSyntaxError(extendsListType.getCanonicalText());
            }
            allMethods.addAll(Arrays.asList(getAllMethods(extendCls)));
        }
        allMethods.addAll(Arrays.asList(methods));
        return allMethods.toArray(new PsiMethod[0]);
    }

    public static PsiField[] getAllFieldsByPsiClass(PsiClass psiClass) {
        PsiField[] fields = psiClass.getFields();
        PsiClassType[] extendsListTypes = psiClass.getExtendsListTypes();
        if (extendsListTypes.length == 0) {
            return filterPsiField(Arrays.asList(fields));
        }
        List<PsiField> allFields = new ArrayList<>();
        for (PsiClassType extendsListType : extendsListTypes) {
            PsiClass extendCls = extendsListType.resolve();
            if (extendCls == null) {
                handleSyntaxError(extendsListType.getCanonicalText());
            }
            allFields.addAll(Arrays.asList(getAllFieldsByPsiClass(extendCls)));
        }
        allFields.addAll(Arrays.asList(fields));
        return filterPsiField(allFields);
    }

    private static PsiField[] filterPsiField(List<PsiField> allFieldList) {
        return allFieldList.stream().filter(psiField -> {
            PsiModifierList modifierList = psiField.getModifierList();
            if (modifierList != null) {
                return !modifierList.hasModifierProperty(PsiModifier.STATIC) && !modifierList.hasModifierProperty(PsiModifier.TRANSIENT);
            }
            return true;
        }).toArray(PsiField[]::new);
    }

    public static boolean isNotSystemClass(PsiClass psiClass) {
        if (psiClass == null) {
            return false;
        }
        String qualifiedName = psiClass.getQualifiedName();
        Set<String> okJavaSet = new HashSet<>();
        okJavaSet.add("java.util.Map.Entry");
        if (okJavaSet.contains(qualifiedName)) {
            return true;
        }
        return qualifiedName != null && !qualifiedName.startsWith("java.");
    }

    public static boolean isValidMethod(PsiMethod method, String... prefixArray) {
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

    public static boolean checkClassHasValidMethod(PsiClass psiClass, String... prefixArray) {
        if (psiClass == null) {
            return false;
        }
        while (isNotSystemClass(psiClass)) {
            for (PsiMethod method : psiClass.getMethods()) {
                if (isValidMethod(method, prefixArray)) {
                    return true;
                }
            }
            psiClass = psiClass.getSuperClass();
        }
        return false;
    }

    public static boolean checkClassHasValidGetter(PsiClass psiClass) {
        return checkClassHasValidMethod(psiClass, GenerateConst.GET_METHOD_PREFIX, GenerateConst.IS_METHOD_PREFIX);
    }

    public static boolean checkClassHasValidSetter(PsiClass psiClass) {
        return checkClassHasValidMethod(psiClass, GenerateConst.SET_METHOD_PREFIX);
    }

    public static List<PsiMethod> getMethodByPrefix(PsiClass psiClass, String... prefixArray) {
        List<PsiMethod> methodList = new ArrayList<>();
        Set<String> methodNameSet = new HashSet<>();
        while (isNotSystemClass(psiClass)) {
            addMethodToList(psiClass, methodList, methodNameSet,  prefixArray);
            psiClass = psiClass.getSuperClass();
        }
        return methodList;
    }

    public static void addMethodToList(PsiClass psiClass, List<PsiMethod> methodList, Set<String> methodNameSet, String... prefixArray) {
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

    public static List<PsiMethod> getGetterMethod(PsiClass psiClass) {
        return getMethodByPrefix(psiClass, GenerateConst.GET_METHOD_PREFIX, GenerateConst.IS_METHOD_PREFIX);
    }

    public static List<PsiMethod> getSetterMethod(PsiClass psiClass) {
        return getMethodByPrefix(psiClass, GenerateConst.SET_METHOD_PREFIX);
    }

    public static Map<String, PsiMethod> getMethodMap(List<PsiMethod> methodList) {
        return methodList.stream().collect(Collectors.toMap(PsiMethod::getName, Function.identity(), (m1, m2) -> m1));
    }

    /**
     * 判断该类是否为Controller或者Feign
     * @param psiClass 类
     * @return true: 是  false: 否
     */
    public static boolean isControllerOrFeign(PsiClass psiClass) {
        // controller 和 feign 处理, 其他不处理
        PsiAnnotation psiAnnotation = psiClass.getAnnotation("org.springframework.stereotype.Controller");
        if (psiAnnotation == null) {
            psiAnnotation = psiClass.getAnnotation("org.springframework.web.bind.annotation.RestController");
        }
        // 若类不是 Controller 则不显示
        if (psiAnnotation != null) {
            return true;
        }
        // 若不是微服务 feign 修饰接口则不显示
        return psiClass.getAnnotation("org.springframework.cloud.openfeign.FeignClient") != null;
    }

    /**
     * 判断此类是否为普通接口
     *
     * @param psiClass 类
     * @param project  项目
     * @return 是否为普通接口
     */
    public static boolean isNormalInterface(PsiClass psiClass, Project project) {
        if (psiClass.isInterface()) {
            String name = psiClass.getQualifiedName();
            String qNameOfList = "java.util.List";
            String qNameOfMap = "java.util.Map";
            String qNameOfSet = "java.util.Set";
            boolean psiClassFromList = PsiUtil.isPsiClassFromXxx(psiClass, project, qNameOfList);
            boolean psiClassFromMap = PsiUtil.isPsiClassFromXxx(psiClass, project, qNameOfMap);
            boolean psiClassFromSet = PsiUtil.isPsiClassFromXxx(psiClass, project, qNameOfSet);
            // 不是 (list,map,set)
            return !(psiClassFromList
                    || psiClassFromMap
                    || psiClassFromSet);
        }
        return false;
    }

}
