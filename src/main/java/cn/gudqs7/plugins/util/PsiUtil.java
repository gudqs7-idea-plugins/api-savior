package cn.gudqs7.plugins.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.search.GlobalSearchScope;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wenquan
 * @date 2021/9/30
 */
public class PsiUtil {

    // =================     泛型相关工具     ====================

    public static final Map<String, PsiType[]> GENERIC_MAP = new HashMap<>();

    public static void resolvePsiClassParameter(PsiClassType psiClassReferenceType) {
        PsiClass psiClass = psiClassReferenceType.resolve();
        if (psiClass == null) {
            handleSyntaxError(psiClassReferenceType.getCanonicalText());
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

    public static void clearGeneric() {
        GENERIC_MAP.clear();
    }

    public static PsiType getRealPsiType0(String ownerQname, int index, Project project, PsiType defaultVal) {
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

    public static boolean isPsiTypeFromParameter(PsiType psiFieldType) {
        if (psiFieldType instanceof PsiClassReferenceType) {
            PsiClassReferenceType psiClassReferenceType = (PsiClassReferenceType) psiFieldType;
            PsiClass resolveClass = psiClassReferenceType.resolve();
            if (resolveClass == null) {
                handleSyntaxError(psiClassReferenceType.getCanonicalText());
            }
            return resolveClass instanceof PsiTypeParameter;
        }
        return false;
    }

    public static boolean isPsiTypeFromCollection(PsiType psiFieldType, Project project) {
        return isPsiTypeFromXxx(psiFieldType, project, "java.util.Collection");
    }

    public static boolean isPsiTypeFromSet(PsiType psiFieldType, Project project) {
        return isPsiTypeFromXxx(psiFieldType, project, "java.util.Set");
    }

    public static boolean isPsiTypeFromList(PsiType psiFieldType, Project project) {
        return isPsiTypeFromXxx(psiFieldType, project, "java.util.List");
    }

    public static boolean isPsiTypeFromMap(PsiType psiFieldType, Project project) {
        return isPsiTypeFromXxx(psiFieldType, project, "java.util.Map");
    }

    public static boolean isPsiTypeFromXxx(PsiType psiFieldType, Project project, String qNameOfXxx) {
        boolean isReferenceType = psiFieldType instanceof PsiClassReferenceType;
        if (isReferenceType) {
            PsiClassReferenceType psiClassReferenceType = (PsiClassReferenceType) psiFieldType;
            PsiClass resolveClass = psiClassReferenceType.resolve();
            if (resolveClass == null) {
                handleSyntaxError(psiClassReferenceType.getCanonicalText());
            }
            return isPsiClassFromXxx(resolveClass, project, qNameOfXxx);
        }
        return false;
    }

    public static boolean isPsiClassFromXxx(PsiClass psiClass, Project project, String qNameOfXxx) {
        String qNameOfClass = psiClass.getQualifiedName();
        if (StringUtils.isBlank(qNameOfClass)) {
            return false;
        }
        PsiClass xxxClass = findOnePsiClassByClassName(qNameOfXxx, project);
        PsiClassType psiType = PsiType.getTypeByName(qNameOfClass, project, GlobalSearchScope.allScope(project));
        PsiClassType xxxType = PsiType.getTypeByName(qNameOfXxx, project, GlobalSearchScope.allScope(project));
        boolean assignableFromXxx = xxxType.isAssignableFrom(psiType);
        boolean isXxxType = psiClass.isInheritor(xxxClass, true);
        return assignableFromXxx || isXxxType;
    }


    // =================     其他相关工具     ====================

    public static void handleSyntaxError(String code) throws RuntimeException {
        throw new RuntimeException("您的代码可能存在语法错误, 无法为您生成代码, 参考信息: " + code);
    }

    public static PsiClass findOnePsiClassByClassName(String qualifiedClassName, Project project) {
        return JavaPsiFacade.getInstance(project).findClass(qualifiedClassName, GlobalSearchScope.allScope(project));
    }

}
