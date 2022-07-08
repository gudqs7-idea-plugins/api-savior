package cn.gudqs7.plugins.common.util.structure;

import cn.gudqs7.plugins.common.util.jetbrain.ExceptionUtil;
import cn.gudqs7.plugins.common.util.jetbrain.PsiSearchUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.search.GlobalSearchScope;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 泛型, PsiType, PsiClass 相关类型判断工具类
 *
 * @author wq
 * @date 2022/6/4
 */
public class PsiTypeUtil {

    public static final Map<String, PsiType[]> GENERIC_MAP = new HashMap<>();

    /**
     * 提前解析类上的泛型信息到 GENERIC_MAP, 以确保后续获取泛型信息时能正确返回
     *
     * @param psiClassReferenceType 类
     */
    public static void resolvePsiClassParameter(PsiClassType psiClassReferenceType) {
        PsiClass psiClass = psiClassReferenceType.resolve();
        if (psiClass == null) {
            ExceptionUtil.handleSyntaxError(psiClassReferenceType.getCanonicalText());
        }
        String qualifiedName = psiClass.getQualifiedName();
        PsiType[] parameters = psiClassReferenceType.getParameters();
        if (parameters.length > 0) {
            GENERIC_MAP.put(qualifiedName, parameters);
        }
        PsiClassType[] extendsListTypes = psiClass.getExtendsListTypes();
        if (extendsListTypes.length > 0) {
            for (PsiClassType extendsListType : extendsListTypes) {
                resolvePsiClassParameter(extendsListType);
            }
        }
    }

    /**
     * 动作结束后清除, 避免下次动作开始时代码修改造成的数据不一致
     */
    public static void clearGeneric() {
        GENERIC_MAP.clear();
    }


    /**
     * 判断 PsiType 是否为泛型, 若是则返回泛型的真实类型
     *
     * @param psiFieldType PsiType
     * @param project      项目
     * @param defaultVal   不是泛型或取不到泛型数据时的默认值
     * @return 泛型的真实类型或默认值
     */
    public static PsiType getRealPsiType(PsiType psiFieldType, Project project, PsiType defaultVal) {
        PsiClassReferenceType psiClassReferenceType = (PsiClassReferenceType) psiFieldType;
        PsiClass resolveClass = psiClassReferenceType.resolve();
        if (resolveClass instanceof PsiTypeParameter) {
            PsiTypeParameter typeParameter = (PsiTypeParameter) resolveClass;
            int index = typeParameter.getIndex();
            PsiTypeParameterListOwner owner = typeParameter.getOwner();
            if (owner instanceof PsiClass) {
                PsiClass psiClass = (PsiClass) owner;
                String qualifiedName = psiClass.getQualifiedName();
                return getRealPsiType0(qualifiedName, index, project, defaultVal);
            }
        }
        return defaultVal;
    }

    /**
     * 获取泛型真实类型后的类型名(兼容处理 List/Collection/Map 等支持泛型的类型)
     *
     * @param psiType        类型
     * @param project        项目
     * @param typeNameFormat 递归传递的格式化辅助
     * @return 类型名
     */
    public static String getRealPsiTypeName(PsiType psiType, Project project, String typeNameFormat) {
        if (isPsiTypeFromParameter(psiType)) {
            PsiType realPsiType = getRealPsiType(psiType, project, psiType);
            if (psiType == realPsiType) {
                String typeName = "Object";
                typeName = String.format(typeNameFormat, typeName);
                return typeName;
            } else {
                psiType = realPsiType;
            }
        }
        boolean isArrayType = psiType instanceof PsiArrayType;
        if (isArrayType) {
            PsiArrayType psiArrayType = (PsiArrayType) psiType;
            PsiType componentType = psiArrayType.getComponentType();
            String typeFormat = String.format(typeNameFormat, "%s[]");
            return getRealPsiTypeName(componentType, project, typeFormat);
        }

        if (psiType instanceof PsiClassReferenceType) {
            PsiClassReferenceType psiClassReferenceType = (PsiClassReferenceType) psiType;
            PsiClass resolveClass = psiClassReferenceType.resolve();
            PsiType[] parameters = psiClassReferenceType.getParameters();
            boolean hasGenericParameter = parameters.length > 0;
            if (isPsiTypeFromList(psiType, project)) {
                if (hasGenericParameter) {
                    PsiType elementType = parameters[0];
                    String typeFormat = String.format(typeNameFormat, "List<%s>");
                    return getRealPsiTypeName(elementType, project, typeFormat);
                }
            }
            if (isPsiTypeFromSet(psiType, project)) {
                if (hasGenericParameter) {
                    PsiType elementType = parameters[0];
                    String typeFormat = String.format(typeNameFormat, "Set<%s>");
                    return getRealPsiTypeName(elementType, project, typeFormat);
                }
            }
            if (isPsiTypeFromCollection(psiType, project)) {
                if (hasGenericParameter) {
                    PsiType elementType = parameters[0];
                    String typeFormat = String.format(typeNameFormat, "Collection<%s>");
                    return getRealPsiTypeName(elementType, project, typeFormat);
                }
            }
            if (isPsiTypeFromMap(psiType, project)) {
                if (parameters.length > 1) {
                    PsiType keyType = parameters[0];
                    PsiType valueType = parameters[1];
                    String keyTypeName;
                    if (isPsiTypeFromParameter(keyType)) {
                        keyType = getRealPsiType(keyType, project, keyType);
                    }
                    keyTypeName = keyType.getPresentableText();
                    String typeFormat = String.format(typeNameFormat, "Map<" + keyTypeName + ", %s>");
                    return getRealPsiTypeName(valueType, project, typeFormat);
                }
            }
        }
        String typeName = psiType.getPresentableText();
        typeName = String.format(typeNameFormat, typeName);
        return typeName;
    }

    /**
     * 判断类型是否为泛型
     *
     * @param psiFieldType 类型
     * @return 类型是否为泛型
     */
    public static boolean isPsiTypeFromParameter(PsiType psiFieldType) {
        if (psiFieldType instanceof PsiClassReferenceType) {
            PsiClassReferenceType psiClassReferenceType = (PsiClassReferenceType) psiFieldType;
            PsiClass resolveClass = psiClassReferenceType.resolve();
            if (resolveClass == null) {
                ExceptionUtil.handleSyntaxError(psiClassReferenceType.getCanonicalText());
            }
            return resolveClass instanceof PsiTypeParameter;
        }
        return false;
    }

    /**
     * 判断类型 is a Collection
     *
     * @param psiFieldType 类型
     * @param project      项目
     * @return 类型 is a Collection
     */
    public static boolean isPsiTypeFromCollection(PsiType psiFieldType, Project project) {
        return isPsiTypeFromXxx(psiFieldType, project, "java.util.Collection");
    }

    /**
     * 判断类型 is a Set
     *
     * @param psiFieldType 类型
     * @param project      项目
     * @return 类型 is a Set
     */
    public static boolean isPsiTypeFromSet(PsiType psiFieldType, Project project) {
        return isPsiTypeFromXxx(psiFieldType, project, "java.util.Set");
    }

    /**
     * 判断类型 is a List
     *
     * @param psiFieldType 类型
     * @param project      项目
     * @return 类型 is a List
     */
    public static boolean isPsiTypeFromList(PsiType psiFieldType, Project project) {
        return isPsiTypeFromXxx(psiFieldType, project, "java.util.List");
    }

    /**
     * 判断类型 is a Map
     *
     * @param psiFieldType 类型
     * @param project      项目
     * @return 类型 is a Map
     */
    public static boolean isPsiTypeFromMap(PsiType psiFieldType, Project project) {
        return isPsiTypeFromXxx(psiFieldType, project, "java.util.Map");
    }

    /**
     * 判断类型 is a Xxx
     *
     * @param psiFieldType 类型
     * @param project      项目
     * @param qNameOfXxx   类全限定名(Xxx)
     * @return 类型 is a Xxx
     */
    public static boolean isPsiTypeFromXxx(PsiType psiFieldType, Project project, String qNameOfXxx) {
        boolean isReferenceType = psiFieldType instanceof PsiClassReferenceType;
        if (isReferenceType) {
            PsiClassReferenceType psiClassReferenceType = (PsiClassReferenceType) psiFieldType;
            PsiClass resolveClass = psiClassReferenceType.resolve();
            if (resolveClass == null) {
                ExceptionUtil.handleSyntaxError(psiClassReferenceType.getCanonicalText());
            }
            return isPsiClassFromXxx(resolveClass, project, qNameOfXxx);
        }
        return false;
    }

    /**
     * 判断类 is a Xxx
     *
     * @param psiClass   类型
     * @param project    项目
     * @param qNameOfXxx 类全限定名(Xxx)
     * @return 类 is a Xxx
     */
    public static boolean isPsiClassFromXxx(PsiClass psiClass, Project project, String qNameOfXxx) {
        String qNameOfClass = psiClass.getQualifiedName();
        if (StringUtils.isBlank(qNameOfClass)) {
            return false;
        }
        PsiClass xxxClass = PsiSearchUtil.findPsiClassByQname(project, qNameOfXxx, true);
        PsiClassType psiType = PsiType.getTypeByName(qNameOfClass, project, GlobalSearchScope.allScope(project));
        PsiClassType xxxType = PsiType.getTypeByName(qNameOfXxx, project, GlobalSearchScope.allScope(project));
        boolean assignableFromXxx = xxxType.isAssignableFrom(psiType);
        boolean isXxxType = psiClass.isInheritor(xxxClass, true);
        return assignableFromXxx || isXxxType;
    }

    /**
     * 得到类名
     *
     * @param psiType      psi类型
     * @param contextClass 上下文类, 若内部类恰好位于此类, 则可不添加前缀
     * @return {@link String}
     */
    @Nullable
    public static String getClassName(@NotNull PsiType psiType, PsiClass contextClass) {
        if (psiType instanceof PsiClassReferenceType) {
            PsiClassReferenceType classReferenceType = (PsiClassReferenceType) psiType;
            PsiClass psiClass = classReferenceType.resolve();
            if (psiClass == null || psiClass.getName() == null) {
                return null;
            }
            String contextClassName = null;
            if (contextClass != null) {
                contextClassName = contextClass.getQualifiedName();
            }
            // 若上下文位于此类, 则不需要拼接前缀
            if (Objects.equals(contextClassName, psiClass.getQualifiedName())) {
                return null;
            }
            StringBuilder className = new StringBuilder(psiClass.getName());
            while (true) {
                PsiClass containingClass = psiClass.getContainingClass();
                if (containingClass == null) {
                    break;
                }
                // 若当前上下文类与之相等, 则可忽略此类型, 并跳出循环
                if (Objects.equals(contextClassName, containingClass.getQualifiedName())) {
                    break;
                }
                className.insert(0, containingClass.getName() + ".");
                psiClass = containingClass;
            }
            return className.toString();
        }
        return null;
    }

    private static PsiType getRealPsiType0(String ownerQname, int index, Project project, PsiType defaultVal) {
        PsiType[] psiTypes = GENERIC_MAP.get(ownerQname);
        if (psiTypes != null && psiTypes.length > 0) {
            if (index >= 0 && index <= psiTypes.length - 1) {
                PsiType psiType = psiTypes[index];
                if (isPsiTypeFromParameter(psiType)) {
                    return getRealPsiType(psiType, project, defaultVal);
                } else {
                    return psiType;
                }
            } else {
                return PsiType.getTypeByName("java.lang.Object", project, GlobalSearchScope.allScope(project));
            }
        } else {
            return PsiType.getTypeByName("java.lang.Object", project, GlobalSearchScope.allScope(project));
        }
    }
}
