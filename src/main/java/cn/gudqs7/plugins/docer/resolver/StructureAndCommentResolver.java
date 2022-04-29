package cn.gudqs7.plugins.docer.resolver;

import cn.gudqs7.plugins.docer.annotation.AnnotationHolder;
import cn.gudqs7.plugins.docer.constant.FieldType;
import cn.gudqs7.plugins.docer.constant.MapKeyConstant;
import cn.gudqs7.plugins.docer.constant.StructureType;
import cn.gudqs7.plugins.docer.pojo.StructureAndCommentInfo;
import cn.gudqs7.plugins.docer.pojo.annotation.CommentInfo;
import cn.gudqs7.plugins.docer.savior.base.BaseSavior;
import cn.gudqs7.plugins.docer.theme.Theme;
import cn.gudqs7.plugins.docer.util.DataHolder;
import cn.gudqs7.plugins.util.PsiClassUtil;
import cn.gudqs7.plugins.util.PsiUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author WQ
 * @date 2022/4/4
 */
public class StructureAndCommentResolver extends BaseSavior implements IStructureAndCommentResolver {

    private Project project;

    private final ConcurrentHashMap<String, StructureAndCommentInfo> earlyCache = new ConcurrentHashMap<>(16);
    private final ConcurrentHashMap<String, StructureAndCommentInfo> psiClassCache = new ConcurrentHashMap<>(16);

    public StructureAndCommentResolver(Theme theme) {
        super(theme);
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public StructureAndCommentInfo resolveFromClass(PsiClassReferenceType psiClassReferenceType) {
        if (psiClassReferenceType == null) {
            return null;
        }
        StructureAndCommentInfo root = resolveFromClass0(psiClassReferenceType, "", 1);
        psiClassCache.clear();
        earlyCache.clear();
        return root;
    }

    @Override
    public StructureAndCommentInfo resolveFromParameter(PsiParameter psiParameter) {
        if (psiParameter == null) {
            return null;
        }
        PsiType psiFieldType = psiParameter.getType();
        String fieldName = psiParameter.getName();

        int level = 0;
        // 普通类型
        String typeName = psiFieldType.getPresentableText();
        if (isJavaBaseType(typeName) || "Object".equals(typeName) || PsiUtil.isPsiTypeFromMap(psiFieldType, project)) {
            level = 1;
        }
        AnnotationHolder annotationHolder = AnnotationHolder.getPsiParameterHolder(psiParameter);
        CommentInfo commentInfo = annotationHolder.getCommentInfo();
        StructureAndCommentInfo structureAndCommentInfo = resolveByPsiType(null, fieldName, psiFieldType, commentInfo, null, MapKeyConstant.FIELD_PREFIX_INIT, level);
        structureAndCommentInfo.setType(StructureType.PSI_PARAM.getType());
        structureAndCommentInfo.setPsiParameter(psiParameter);
        return structureAndCommentInfo;
    }

    @Override
    public StructureAndCommentInfo resolveFromParameterList(PsiParameterList parameterList) {
        if (parameterList == null || parameterList.isEmpty()) {
            return null;
        }

        PsiParameter[] parameterListParameters = parameterList.getParameters();
        // 当 level = 0 时, 字段详细说明文档 不显示此参数的文档
        int level;
        if (parameterListParameters.length == 1) {
            PsiParameter parameterListParameter = parameterListParameters[0];
            PsiType psiFieldType = parameterListParameter.getType();
            level = 0;
            // 普通类型
            String typeName = psiFieldType.getPresentableText();
            if (isJavaBaseType(typeName) || "Object".equals(typeName) || PsiUtil.isPsiTypeFromMap(psiFieldType, project)) {
                level = 1;
            }
        } else {
            // 若有多个参数, 则不能为 0.
            level = 1;
        }

        StructureAndCommentInfo root = new StructureAndCommentInfo();
        root.setType(StructureType.PSI_PARAM_LIST.getType());
        root.setPsiParameterList(parameterList);
        root.setFieldType("Params");
        root.setFieldTypeCode(FieldType.PARAM_LIST.getType());
        root.setLevel(level);

        for (PsiParameter psiParameter : parameterListParameters) {
            String fieldName = psiParameter.getName();
            PsiType psiFieldType = psiParameter.getType();
            AnnotationHolder annotationHolder = AnnotationHolder.getPsiParameterHolder(psiParameter);
            CommentInfo commentInfo = annotationHolder.getCommentInfo();
            StructureAndCommentInfo structureAndCommentInfo = resolveByPsiType(root, fieldName, psiFieldType, commentInfo, null, MapKeyConstant.FIELD_PREFIX_INIT, level);
            if (structureAndCommentInfo == null) {
                continue;
            }
            structureAndCommentInfo.setType(StructureType.PSI_PARAM.getType());
            structureAndCommentInfo.setPsiParameter(psiParameter);
            root.addChild(fieldName, structureAndCommentInfo);
        }
        psiClassCache.clear();
        earlyCache.clear();
        return root;
    }

    @Override
    public StructureAndCommentInfo resolveFromReturnVal(PsiTypeElement returnTypeElement) {
        if (returnTypeElement == null) {
            return null;
        }
        PsiType returnType = returnTypeElement.getType();
        if ("void".equals(returnType.getPresentableText())) {
            StructureAndCommentInfo structureAndCommentInfo = new StructureAndCommentInfo();
            structureAndCommentInfo.setReturnType(true);
            structureAndCommentInfo.setFieldType("Void");
            structureAndCommentInfo.setFieldTypeCode(FieldType.RETURN.getType());
            structureAndCommentInfo.setType(StructureType.PSI_RETURN.getType());
            structureAndCommentInfo.setReturnTypeElement(returnTypeElement);
            return structureAndCommentInfo;
        }
        int level = 0;
        // 普通类型
        String typeName = returnType.getPresentableText();
        if (isJavaBaseType(typeName) || "Object".equals(typeName) || PsiUtil.isPsiTypeFromMap(returnType, project)) {
            level = 1;
        }

        AnnotationHolder psiReturnTypeHolder = AnnotationHolder.getPsiReturnTypeHolder(returnTypeElement);
        CommentInfo commentInfo = psiReturnTypeHolder.getCommentInfo();
        StructureAndCommentInfo structureAndCommentInfo = resolveByPsiType(null, "", returnType, commentInfo, null, MapKeyConstant.FIELD_PREFIX_INIT, level);
        structureAndCommentInfo.setReturnType(true);
        structureAndCommentInfo.setType(StructureType.PSI_RETURN.getType());
        structureAndCommentInfo.setReturnTypeElement(returnTypeElement);

        psiClassCache.clear();
        earlyCache.clear();
        return structureAndCommentInfo;
    }

    private StructureAndCommentInfo resolveFromClass0(PsiClassReferenceType psiClassReferenceType, String fieldPrefix, int level) {
        if (psiClassReferenceType == null) {
            return null;
        }
        PsiClass psiClass = psiClassReferenceType.resolve();
        if (psiClass == null) {
            PsiUtil.handleSyntaxError(psiClassReferenceType.getCanonicalText());
        }
        String qualifiedName = psiClass.getQualifiedName();
        PsiUtil.resolvePsiClassParameter(psiClassReferenceType);
        if (qualifiedName != null) {
            if (psiClassCache.containsKey(qualifiedName)) {
                return null;
            }
            if (earlyCache.containsKey(qualifiedName)) {
                return null;
            }
        }
        String clazzTypeName = psiClass.getName();
        if (isJavaBaseType(clazzTypeName) || "Object".equals(clazzTypeName)) {
            System.out.println("resolveFromClass0 - shouldn't in! type=" + clazzTypeName);
            return null;
        }

        AnnotationHolder classAnnotationHolder = AnnotationHolder.getPsiClassHolder(psiClass);
        CommentInfo classCommentInfo = classAnnotationHolder.getCommentInfo();
        if (classCommentInfo == null) {
            return null;
        }
        StructureAndCommentInfo root = new StructureAndCommentInfo();
        root.setFieldType(clazzTypeName);
        root.setFieldTypeCode(FieldType.POJO.getType());
        root.setType(StructureType.PSI_CLASS.getType());
        root.setPsiClass(psiClass);
        root.setLevel(level);
        root.setCommentInfo(classCommentInfo);

        if (qualifiedName != null) {
            earlyCache.put(qualifiedName, root);
        }
        PsiField[] typeDeclaredFields = PsiClassUtil.getAllFieldsByPsiClass(psiClass);
        for (PsiField psiField : typeDeclaredFields) {
            String fieldName = psiField.getName();
            PsiType psiFieldType = psiField.getType();
            AnnotationHolder annotationHolder = AnnotationHolder.getPsiFieldHolder(psiField);
            CommentInfo commentInfo = annotationHolder.getCommentInfo();
            fieldName = commentInfo.getName(fieldName);
            if (checkHiddenRequest(fieldPrefix, fieldName)) {
                continue;
            }

            StructureAndCommentInfo child =  resolveByPsiType(root, fieldName, psiFieldType, commentInfo, psiClassReferenceType, fieldPrefix, level);
            if (child == null) {
                continue;
            }
            child.setType(StructureType.PSI_FIELD.getType());
            child.setPsiField(psiField);
            root.addChild(fieldName, child);
        }
        if (qualifiedName != null) {
            psiClassCache.put(qualifiedName, root);
        }
        return root;
    }

    private StructureAndCommentInfo resolveByPsiType(StructureAndCommentInfo parent, String fieldName, PsiType psiFieldType, CommentInfo commentInfo, PsiClassReferenceType parentPsiClassReferenceType, String fieldPrefix, int level) {
        if (psiFieldType == null) {
            return null;
        }
        if (parentPsiClassReferenceType != null) {
            PsiClass parentPsiClass = parentPsiClassReferenceType.resolve();
            if (parentPsiClass == null) {
                PsiUtil.handleSyntaxError(parentPsiClassReferenceType.getCanonicalText());
            }
            String qualifiedName = parentPsiClass.getQualifiedName();
            String canonicalText = psiFieldType.getCanonicalText();
            if (Objects.equals(qualifiedName, canonicalText)) {
                // 自己依赖自己
                return null;
            }
        }
        return resolveByPsiType0(FieldType.BASE.getType(), fieldName, psiFieldType, commentInfo, "%s", fieldPrefix, level);
    }

    @SuppressWarnings("AlibabaMethodTooLong")
    private StructureAndCommentInfo resolveByPsiType0(int fieldTypeCode, String fieldName, PsiType psiFieldType, CommentInfo commentInfo, String typeNameFormat, String fieldPrefix, int level) {
        boolean hidden = commentInfo.isHidden(false);
        fieldName = commentInfo.getName(fieldName);
        hidden = theme.handleHidden(fieldName, psiFieldType, hidden);
        if (hidden) {
            return null;
        }
        String psiFieldTypeName = psiFieldType.getPresentableText();
        String fieldTypeName = String.format(typeNameFormat, psiFieldTypeName);
        StructureAndCommentInfo structureAndCommentInfo = new StructureAndCommentInfo();
        structureAndCommentInfo.setLeaf(true);
        structureAndCommentInfo.setLevel(level);
        structureAndCommentInfo.setFieldName(fieldName);
        structureAndCommentInfo.setCommentInfo(commentInfo);
        structureAndCommentInfo.setFieldType(fieldTypeName);
        structureAndCommentInfo.setOriginalFieldType(psiFieldTypeName);
        structureAndCommentInfo.setFieldTypeCode(fieldTypeCode);

        // 普通字段, 即刻返回
        String typeSimpleName = psiFieldType.getPresentableText();
        if (isJavaBaseTypeOrObject(typeSimpleName)) {
            return structureAndCommentInfo;
        }

        boolean isArrayType = psiFieldType instanceof PsiArrayType;
        if (isArrayType) {
            PsiArrayType psiArrayType = (PsiArrayType) psiFieldType;
            PsiType componentType = psiArrayType.getComponentType();
            String newTypeNameFormat = String.format(typeNameFormat, "%s[]");
            return resolveByPsiType0(FieldType.ARRAY.getType(), fieldName, componentType, commentInfo, newTypeNameFormat, fieldPrefix, level);
        }

        boolean isReferenceType = psiFieldType instanceof PsiClassReferenceType;
        // 引用(枚举/对象/List/Map)
        if (isReferenceType) {
            PsiClassReferenceType psiClassReferenceType = (PsiClassReferenceType) psiFieldType;
            PsiClass resolveClass = psiClassReferenceType.resolve();
            if (resolveClass == null) {
                PsiUtil.handleSyntaxError(psiClassReferenceType.getCanonicalText());
            }

            PsiType[] parameters = psiClassReferenceType.getParameters();
            if (parameters.length > 0) {
                StringBuilder name = new StringBuilder();
                for (PsiType parameter : parameters) {
                    String realTypeName = PsiUtil.getRealPsiTypeName(parameter, project, "%s");
                    name.append(realTypeName).append(", ");
                }
                if (name.length() != 0) {
                    String name0 = name.substring(0, name.length() - 2);
                    typeSimpleName = typeSimpleName.substring(0, typeSimpleName.indexOf("<")) + "<" + name0 + ">";
                }
                // 此处 put 只影响普通 class, 不影响 list, map 等
                structureAndCommentInfo.setFieldType(typeSimpleName);
            }


            PsiType realPsiType = PsiUtil.getRealPsiType(psiFieldType, project, null);
            if (realPsiType != null) {
                String newTypeNameFormat = String.format(typeNameFormat, "%s");
                return resolveByPsiType0(fieldTypeCode, fieldName, realPsiType, commentInfo, newTypeNameFormat, fieldPrefix, level);
            }

            // 枚举
            if (resolveClass.isEnum()) {
                return structureAndCommentInfo;
            }

            // List
            if (PsiUtil.isPsiTypeFromList(psiFieldType, project)) {
                return getStructureAndCommentInfoByCollection(fieldName, commentInfo, typeNameFormat, fieldPrefix, level,
                        structureAndCommentInfo, parameters, "List<%s>", FieldType.LIST);
            }

            // Set
            if (PsiUtil.isPsiTypeFromSet(psiFieldType, project)) {
                return getStructureAndCommentInfoByCollection(fieldName, commentInfo, typeNameFormat, fieldPrefix, level,
                        structureAndCommentInfo, parameters, "Set<%s>", FieldType.SET);
            }

            // Collection 放在后面判断, 优先级低一些
            if (PsiUtil.isPsiTypeFromCollection(psiFieldType, project)) {
                return getStructureAndCommentInfoByCollection(fieldName, commentInfo, typeNameFormat, fieldPrefix, level,
                        structureAndCommentInfo, parameters, "Collection<%s>", FieldType.COLLECTION);
            }

            // 判断是否为 File
            if (PsiUtil.isPsiTypeFromXxx(psiFieldType, project, AnnotationHolder.QNAME_OF_MULTIPART_FILE)) {
                structureAndCommentInfo.setFieldTypeCode(FieldType.FILE.getType());
            }

            // Map
            if (PsiUtil.isPsiTypeFromMap(psiFieldType, project)) {
                if (parameters.length > 1) {
                    PsiType keyType = parameters[0];
                    PsiType valueType = parameters[1];
                    String keyTypeName;
                    if (PsiUtil.isPsiTypeFromParameter(keyType)) {
                        keyType = PsiUtil.getRealPsiType(keyType, project, keyType);
                    }
                    keyTypeName = keyType.getPresentableText();
                    String newTypeNameFormat = String.format(typeNameFormat, "Map<" + keyTypeName + ", %s>");
                    return resolveByPsiType0(FieldType.MAP.getType(), fieldName, valueType, commentInfo, newTypeNameFormat, fieldPrefix, level);
                } else {
                    return structureAndCommentInfo;
                }
            }

            if (!PsiUtil.isPsiTypeFromParameter(psiFieldType)) {
                // 普通对象
                String fieldPrefix0 = fieldPrefix + fieldName + ".";
                if (MapKeyConstant.FIELD_PREFIX_INIT.equals(fieldPrefix)) {
                    fieldPrefix0 = "";
                }
                StructureAndCommentInfo structureAndCommentInfoChild = resolveFromClass0(psiClassReferenceType, fieldPrefix0, level + 1);
                if (structureAndCommentInfoChild != null && structureAndCommentInfoChild.getChildren().size() > 0) {
                    structureAndCommentInfo.setLeaf(false);
                    structureAndCommentInfo.copyChild(structureAndCommentInfoChild.getChildren());
                    if (fieldTypeCode == 1) {
                        structureAndCommentInfo.setFieldTypeCode(FieldType.POJO.getType());
                    }
                }
            }

        } else {
            System.out.println(psiFieldType.getPresentableText() + " ==> not basic type, not ReferenceType");
        }
        return structureAndCommentInfo;
    }

    private StructureAndCommentInfo getStructureAndCommentInfoByCollection(String fieldName, CommentInfo commentInfo, String typeNameFormat, String fieldPrefix, int level, StructureAndCommentInfo structureAndCommentInfo, PsiType[] parameters, String format, FieldType fieldType) {
        if (parameters.length > 0) {
            PsiType elementType = parameters[0];
            String newTypeNameFormat = String.format(typeNameFormat, format);
            return resolveByPsiType0(fieldType.getType(), fieldName, elementType, commentInfo, newTypeNameFormat, fieldPrefix, level);
        } else {
            return structureAndCommentInfo;
        }
    }

    private boolean checkHiddenRequest(String fieldPrefix, String fieldName) {
        String fieldKey = fieldPrefix + fieldName;
        List<String> hiddenRequest = DataHolder.getData(MapKeyConstant.HIDDEN_KEYS);
        List<String> onlyRequest = DataHolder.getData(MapKeyConstant.ONLY_KEYS);
        if (CollectionUtils.isNotEmpty(hiddenRequest)) {
            return hiddenRequest.contains(fieldKey);
        }
        if (CollectionUtils.isNotEmpty(onlyRequest)) {
            return !onlyRequest.contains(fieldKey);
        }
        return false;
    }

}
