package cn.gudqs7.plugins.common.util.structure;

import cn.gudqs7.plugins.common.util.jetbrain.PsiTypeUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

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

}
