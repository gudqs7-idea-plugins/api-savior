package cn.gudqs7.plugins.common.resolver.structure;

import cn.gudqs7.plugins.common.consts.MapKeyConstant;
import cn.gudqs7.plugins.common.enums.FieldType;
import cn.gudqs7.plugins.common.enums.StructureType;
import cn.gudqs7.plugins.common.pojo.resolver.CommentInfo;
import cn.gudqs7.plugins.common.pojo.resolver.StructureAndCommentInfo;
import cn.gudqs7.plugins.common.resolver.comment.AnnotationHolder;
import cn.gudqs7.plugins.common.util.jetbrain.ExceptionUtil;
import cn.gudqs7.plugins.common.util.jetbrain.PsiTypeUtil;
import cn.gudqs7.plugins.common.util.structure.BaseTypeUtil;
import cn.gudqs7.plugins.common.util.structure.FieldJumpUtil;
import cn.gudqs7.plugins.common.util.structure.PsiClassUtil;
import cn.gudqs7.plugins.common.util.structure.ResolverContextHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.impl.compiled.ClsClassImpl;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author WQ
 * @date 2022/4/4
 */
public class StructureAndCommentResolver implements IStructureAndCommentResolver {

    private Project project;

    private final ConcurrentHashMap<String, StructureAndCommentInfo> earlyCache = new ConcurrentHashMap<>(16);
    private final ConcurrentHashMap<String, StructureAndCommentInfo> psiClassCache = new ConcurrentHashMap<>(16);

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
            level = getLevelByPsiType(psiFieldType);
        } else {
            // 若有多个参数, 则不能为 0.
            level = 1;
        }

        StructureAndCommentInfo root = new StructureAndCommentInfo();
        root.setType(StructureType.PSI_PARAM_LIST.getType());
        root.setPsiParameterList(parameterList);
        root.setFieldType("Params");
        root.setOriginalFieldType("Params");
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

    private int getLevelByPsiType(PsiType psiFieldType) {
        // 普通类型 或 List / Map / MultipartFile 时需展示
        String typeName = psiFieldType.getPresentableText();
        if (BaseTypeUtil.isBaseTypeOrObject(psiFieldType)
                || PsiTypeUtil.isPsiTypeFromMap(psiFieldType, project)
                || PsiTypeUtil.isPsiTypeFromCollection(psiFieldType, project)
                || PsiTypeUtil.isPsiTypeFromXxx(psiFieldType, project, AnnotationHolder.QNAME_OF_MULTIPART_FILE)
        ) {
            return 1;
        }
        return 0;
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
            structureAndCommentInfo.setOriginalFieldType("Void");
            structureAndCommentInfo.setFieldTypeCode(FieldType.RETURN_VOID.getType());
            structureAndCommentInfo.setType(StructureType.PSI_RETURN.getType());
            structureAndCommentInfo.setReturnTypeElement(returnTypeElement);
            return structureAndCommentInfo;
        }
        int level = getLevelByPsiType(returnType);

        AnnotationHolder psiReturnTypeHolder = AnnotationHolder.getPsiReturnTypeHolder(returnTypeElement);
        CommentInfo commentInfo = psiReturnTypeHolder.getCommentInfo();
        StructureAndCommentInfo structureAndCommentInfo = resolveByPsiType(null, MapKeyConstant.RETURN_FIELD_NAME, returnType, commentInfo, null, MapKeyConstant.FIELD_PREFIX_INIT, level);
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
            ExceptionUtil.handleSyntaxError(psiClassReferenceType.getCanonicalText());
        }
        //兼容第三方jar包
        psiClass = replacePsiClassIfFromJar(psiClass);
        String qualifiedName = psiClass.getQualifiedName();
        PsiTypeUtil.resolvePsiClassParameter(psiClassReferenceType);
        if (qualifiedName != null) {
            if (psiClassCache.containsKey(qualifiedName)) {
                return null;
            }
            if (earlyCache.containsKey(qualifiedName)) {
                return null;
            }
        }
        String clazzTypeName = psiClass.getName();
        if (BaseTypeUtil.isBaseTypeOrObject(psiClass)) {
            return null;
        }

        AnnotationHolder classAnnotationHolder = AnnotationHolder.getPsiClassHolder(psiClass);
        CommentInfo classCommentInfo = classAnnotationHolder.getCommentInfo();
        if (classCommentInfo == null) {
            return null;
        }
        StructureAndCommentInfo root = new StructureAndCommentInfo();
        root.setFieldType(clazzTypeName);
        root.setOriginalFieldType(clazzTypeName);
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

            StructureAndCommentInfo child = resolveByPsiType(root, fieldName, psiFieldType, commentInfo, psiClassReferenceType, fieldPrefix, level);
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

    @NotNull
    private PsiClass replacePsiClassIfFromJar(@NotNull PsiClass psiClass) {
        if (psiClass instanceof ClsClassImpl) {
            PsiFile containingFile = psiClass.getContainingFile();
            if (containingFile != null) {
                VirtualFile virtualFile = containingFile.getVirtualFile();
                String sourcePath = virtualFile.toString().replace(".jar!", "-sources.jar!");
                // replace .class  to  .java
                sourcePath = sourcePath.substring(0, sourcePath.length() - 5) + "java";
                VirtualFile sourceFile = VirtualFileManager.getInstance().findFileByUrl(sourcePath);
                if (sourceFile != null) {
                    PsiFile psiFile = PsiManager.getInstance(project).findFile(sourceFile);
                    if (psiFile instanceof PsiJavaFile) {
                        PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                        PsiClass[] psiClasses = psiJavaFile.getClasses();
                        for (PsiClass psiClass0 : psiClasses) {
                            if (Objects.equals(psiClass0.getQualifiedName(), psiClass.getQualifiedName())) {
                                return psiClass0;
                            }
                        }
                    }
                } else {
                    System.err.println("findFileByUrl - not found :: " + sourcePath);
                }
            } else {
                System.err.println("containingFile is null :: " + psiClass.getQualifiedName());
            }
        }
        return psiClass;
    }

    private StructureAndCommentInfo resolveByPsiType(StructureAndCommentInfo parent, String fieldName, PsiType psiFieldType, CommentInfo commentInfo, PsiClassReferenceType parentPsiClassReferenceType, String fieldPrefix, int level) {
        if (psiFieldType == null) {
            return null;
        }
        if (parentPsiClassReferenceType != null) {
            PsiClass parentPsiClass = parentPsiClassReferenceType.resolve();
            if (parentPsiClass == null) {
                ExceptionUtil.handleSyntaxError(parentPsiClassReferenceType.getCanonicalText());
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
        hidden = handleHidden(fieldName, psiFieldType, hidden);
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
        structureAndCommentInfo.setOriginalFieldTypeCode(FieldType.BASE.getType());
        structureAndCommentInfo.setPsiType(psiFieldType);

        // 普通字段, 即刻返回
        String typeSimpleName = psiFieldType.getPresentableText();
        if (BaseTypeUtil.isBaseTypeOrObject(psiFieldType)) {
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
                ExceptionUtil.handleSyntaxError(psiClassReferenceType.getCanonicalText());
            }

            PsiType[] parameters = psiClassReferenceType.getParameters();
            if (parameters.length > 0) {
                StringBuilder name = new StringBuilder();
                for (PsiType parameter : parameters) {
                    String realTypeName = PsiTypeUtil.getRealPsiTypeName(parameter, project, "%s");
                    name.append(realTypeName).append(", ");
                }
                if (name.length() != 0) {
                    String name0 = name.substring(0, name.length() - 2);
                    typeSimpleName = typeSimpleName.substring(0, typeSimpleName.indexOf("<")) + "<" + name0 + ">";
                }
                // 此处 put 只影响普通 class, 不影响 list, map 等
                fieldTypeName = String.format(typeNameFormat, typeSimpleName);
                structureAndCommentInfo.setFieldType(fieldTypeName);
            }


            PsiType realPsiType = PsiTypeUtil.getRealPsiType(psiFieldType, project, null);
            if (realPsiType != null) {
                String newTypeNameFormat = String.format(typeNameFormat, "%s");
                return resolveByPsiType0(fieldTypeCode, fieldName, realPsiType, commentInfo, newTypeNameFormat, fieldPrefix, level);
            }

            // 枚举
            if (resolveClass.isEnum()) {
                return structureAndCommentInfo;
            }

            // List
            if (PsiTypeUtil.isPsiTypeFromList(psiFieldType, project)) {
                return getStructureAndCommentInfoByCollection(fieldName, commentInfo, typeNameFormat, fieldPrefix, level,
                        structureAndCommentInfo, parameters, "List<%s>", FieldType.LIST);
            }

            // Set
            if (PsiTypeUtil.isPsiTypeFromSet(psiFieldType, project)) {
                return getStructureAndCommentInfoByCollection(fieldName, commentInfo, typeNameFormat, fieldPrefix, level,
                        structureAndCommentInfo, parameters, "Set<%s>", FieldType.SET);
            }

            // Collection 放在后面判断, 优先级低一些
            if (PsiTypeUtil.isPsiTypeFromCollection(psiFieldType, project)) {
                return getStructureAndCommentInfoByCollection(fieldName, commentInfo, typeNameFormat, fieldPrefix, level,
                        structureAndCommentInfo, parameters, "Collection<%s>", FieldType.COLLECTION);
            }

            // 判断是否为 File
            if (PsiTypeUtil.isPsiTypeFromXxx(psiFieldType, project, AnnotationHolder.QNAME_OF_MULTIPART_FILE)) {
                structureAndCommentInfo.setFieldTypeCode(FieldType.FILE.getType());
                structureAndCommentInfo.setOriginalFieldTypeCode(FieldType.FILE.getType());
            }

            // Map
            if (PsiTypeUtil.isPsiTypeFromMap(psiFieldType, project)) {
                if (parameters.length > 1) {
                    PsiType keyType = parameters[0];
                    PsiType valueType = parameters[1];
                    String keyTypeName;
                    if (PsiTypeUtil.isPsiTypeFromParameter(keyType)) {
                        keyType = PsiTypeUtil.getRealPsiType(keyType, project, keyType);
                    }
                    keyTypeName = keyType.getPresentableText();
                    String newTypeNameFormat = String.format(typeNameFormat, "Map<" + keyTypeName + ", %s>");
                    return resolveByPsiType0(FieldType.MAP.getType(), fieldName, valueType, commentInfo, newTypeNameFormat, fieldPrefix, level);
                } else {
                    return structureAndCommentInfo;
                }
            }

            // FIXME 前面有 realPsiType != null 判断, 此处判断可去除
            if (!PsiTypeUtil.isPsiTypeFromParameter(psiFieldType)) {
                // 普通对象
                String fieldPrefix0 = fieldPrefix + fieldName + ".";
                if (MapKeyConstant.FIELD_PREFIX_INIT.equals(fieldPrefix)) {
                    fieldPrefix0 = "";
                }
                StructureAndCommentInfo structureAndCommentInfoChild = resolveFromClass0(psiClassReferenceType, fieldPrefix0, level + 1);
                if (structureAndCommentInfoChild != null && structureAndCommentInfoChild.getChildren().size() > 0) {
                    structureAndCommentInfo.setLeaf(false);
                    structureAndCommentInfo.copyChild(structureAndCommentInfoChild.getChildren());
                    if (fieldTypeCode == FieldType.BASE.getType()) {
                        structureAndCommentInfo.setFieldTypeCode(FieldType.POJO.getType());
                    }
                    structureAndCommentInfo.setOriginalFieldTypeCode(FieldType.POJO.getType());
                }
            }

        } else {
            System.out.println(psiFieldType.getPresentableText() + " ==> not basic type, not ReferenceType");
        }
        return structureAndCommentInfo;
    }

    private boolean handleHidden(String fieldName, PsiType psiFieldType, boolean oldVal) {
        if (FieldJumpUtil.isFieldNameNeedJump(fieldName)) {
            return true;
        }
        String typeQname = psiFieldType.getCanonicalText();
        if (FieldJumpUtil.isFieldTypeNeedJump(typeQname)) {
            return true;
        }
        return oldVal;
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
        List<String> hiddenKeys = ResolverContextHolder.getData(ResolverContextHolder.HIDDEN_KEYS);
        List<String> onlyKeys = ResolverContextHolder.getData(ResolverContextHolder.ONLY_KEYS);
        if (CollectionUtils.isNotEmpty(hiddenKeys)) {
            return hiddenKeys.contains(fieldKey);
        }
        if (CollectionUtils.isNotEmpty(onlyKeys)) {
            return !onlyKeys.contains(fieldKey);
        }
        return false;
    }

}
