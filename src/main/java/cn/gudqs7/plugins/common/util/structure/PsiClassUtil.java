package cn.gudqs7.plugins.common.util.structure;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static cn.gudqs7.plugins.common.util.jetbrain.ExceptionUtil.handleSyntaxError;

/**
 * PsiClass相关工具
 *
 * @author wenquan
 * @date 2021/9/30
 */
public class PsiClassUtil {

    public static PsiClass getPsiClassByPsiType(PsiType psiType) {
        if (psiType != null) {
            if (psiType instanceof PsiClassReferenceType) {
                PsiClassReferenceType classReferenceType = (PsiClassReferenceType) psiType;
                return classReferenceType.resolve();
            }
        }

        return null;
    }

    @NotNull
    public static List<PsiMethod> getAllMethods(PsiClass psiClass) {
        PsiMethod[] methods = psiClass.getMethods();
        PsiClassType[] extendsListTypes = psiClass.getExtendsListTypes();
        PsiClass[] interfaces = psiClass.getInterfaces();
        List<PsiMethod> methodList = new ArrayList<>(Arrays.asList(methods));
        if (extendsListTypes.length == 0 && interfaces.length == 0) {
            return methodList;
        }

        List<PsiMethod> allMethods = new ArrayList<>();
        for (PsiClassType extendsListType : extendsListTypes) {
            PsiClass extendCls = extendsListType.resolve();
            if (extendCls == null) {
                handleSyntaxError(extendsListType.getCanonicalText());
            }
            allMethods.addAll(getAllMethods(extendCls));
        }
        for (PsiClass anInterface : interfaces) {
            allMethods.addAll(getAllMethods(anInterface));
        }
        allMethods.addAll(methodList);
        return allMethods;
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
        if (StringUtils.isBlank(qualifiedName)) {
            return false;
        }
        Set<String> okJavaSet = new HashSet<>();
        okJavaSet.add("java.util.Map.Entry");
        if (okJavaSet.contains(qualifiedName)) {
            return true;
        }
        return !qualifiedName.startsWith("java.");
    }

    /**
     * 判断该类是否为Controller或者Feign
     *
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
            boolean psiClassFromCollection = PsiTypeUtil.isPsiClassFromXxx(psiClass, project, "java.util.Collection");
            boolean psiClassFromMap = PsiTypeUtil.isPsiClassFromXxx(psiClass, project, "java.util.Map");
            // 不是 (list,map,set)
            return !(psiClassFromCollection
                    || psiClassFromMap);
        }
        return false;
    }

    /**
     * 得到最顶级的类(针对内部类而已)
     *
     * @param psiClass psi类
     * @return {@link PsiClass}
     */
    public static PsiClass getTopmostClass(@NotNull PsiClass psiClass) {
        while (true) {
            PsiClass containingClass = psiClass.getContainingClass();
            if (containingClass == null) {
                return psiClass;
            }
            psiClass = containingClass;
        }
    }

    /**
     * 通过顶级类找到其下的内部类
     *
     * @param topmostClass   顶级类
     * @param innerClassName 内部类全限定名
     * @return {@link PsiClass}
     */
    public static PsiClass findInnerClass(@NotNull PsiClass topmostClass, @NotNull String innerClassName) {
        if (innerClassName.equals(topmostClass.getQualifiedName())) {
            return topmostClass;
        }
        PsiClass[] innerClasses = topmostClass.getInnerClasses();
        if (ArrayUtils.isNotEmpty(innerClasses)) {
            for (PsiClass innerClass : innerClasses) {
                PsiClass innerClass0 = findInnerClass(innerClass, innerClassName);
                if (innerClass0 != null) {
                    return innerClass0;
                }
            }
        }
        return null;
    }

    /**
     * 获得包名
     *
     * @param psiClass psi class
     * @return {@link String} 若为空则未找到包名
     */
    @Nullable
    public static String getPackageName(PsiClass psiClass) {
        PsiElement element = psiClass.getParent();
        if (element instanceof PsiJavaFile) {
            PsiJavaFile psiJavaFile = (PsiJavaFile) element;
            String packageName = psiJavaFile.getPackageName();
            if (StringUtils.isNotBlank(packageName)) {
                return packageName;
            }
        }
        if (element instanceof PsiClass) {
            return getPackageName((PsiClass) element);
        }
        return null;
    }
}
