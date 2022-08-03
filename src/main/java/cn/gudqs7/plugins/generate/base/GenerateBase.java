package cn.gudqs7.plugins.generate.base;

import cn.gudqs7.plugins.common.util.jetbrain.ExceptionUtil;
import cn.gudqs7.plugins.common.util.jetbrain.PsiDocumentUtil;
import cn.gudqs7.plugins.common.util.structure.BaseTypeUtil;
import cn.gudqs7.plugins.common.util.structure.PsiTypeUtil;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.Variable;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * @author WQ
 * @date 2021/10/1
 */
public interface GenerateBase {

    /**
     * 根据参数生成代码
     *
     * @param newImportList 需要引入的 import 列表
     * @param method        方法
     * @return 代码
     */
    @NotNull
    String generateCodeByMethod(Set<String> newImportList, PsiMethod method);

    /**
     * 根据 PsiType 返回其类型
     *
     * @param newImportList  此过程生成的 import 集合
     * @param project        project
     * @param psiFieldType   psiType
     * @param typeNameFormat 应付泛型采取递归的字符串合成-合成占位符
     * @param typeName       实际类型
     * @return 类型
     */
    @SuppressWarnings("AlibabaMethodTooLong")
    default Pair<String, String> handlerByPsiType(Set<String> newImportList, Project project, PsiType psiFieldType, String typeNameFormat, String typeName) {
        String right = "";
        String fieldTypeName = String.format(typeNameFormat, typeName);
        String typeSimpleName = psiFieldType.getPresentableText();
        if (BaseTypeUtil.isBaseTypeOrObject(psiFieldType)) {
            return Pair.of(fieldTypeName, right);
        }

        boolean isArrayType = psiFieldType instanceof PsiArrayType;
        if (isArrayType) {
            PsiArrayType psiArrayType = (PsiArrayType) psiFieldType;
            PsiType componentType = psiArrayType.getComponentType();
            String typeFormat = String.format(typeNameFormat, "%s[]");
            String realTypeName = componentType.getPresentableText();
            return handlerByPsiType(newImportList, project, componentType, typeFormat, realTypeName);
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
                fieldTypeName = typeSimpleName;
            }


            PsiType realPsiType = PsiTypeUtil.getRealPsiType(psiFieldType, project, null);
            if (realPsiType != null) {
                String realTypeName = realPsiType.getPresentableText();
                String typeFormat = String.format(typeNameFormat, "%s");
                return handlerByPsiType(newImportList, project, realPsiType, typeFormat, realTypeName);
            }

            // List
            if (PsiTypeUtil.isPsiTypeFromList(psiFieldType, project)) {
                return getFieldTypeNameByCollection(newImportList, project, typeNameFormat, fieldTypeName, parameters, right,
                        "java.util.List", "List<%s>");
            }

            // Set
            if (PsiTypeUtil.isPsiTypeFromSet(psiFieldType, project)) {
                return getFieldTypeNameByCollection(newImportList, project, typeNameFormat, fieldTypeName, parameters,
                        right, "java.util.Set", "Set<%s>");
            }

            // Collection
            if (PsiTypeUtil.isPsiTypeFromCollection(psiFieldType, project)) {
                return getFieldTypeNameByCollection(newImportList, project, typeNameFormat, fieldTypeName, parameters,
                        right, "java.util.Collection", "Collection<%s>");
            }

            // Map
            if (PsiTypeUtil.isPsiTypeFromMap(psiFieldType, project)) {
                if (parameters.length > 1) {
                    newImportList.add("java.util.Map");
                    PsiType keyType = parameters[0];
                    PsiType valueType = parameters[1];
                    String keyTypeName;
                    if (PsiTypeUtil.isPsiTypeFromParameter(keyType)) {
                        keyType = PsiTypeUtil.getRealPsiType(keyType, project, keyType);
                    }
                    keyTypeName = keyType.getPresentableText();
                    String typeFormat = String.format(typeNameFormat, "Map<" + keyTypeName + ", %s>");
                    String realTypeName = valueType.getPresentableText();
                    return handlerByPsiType(newImportList, project, valueType, typeFormat, realTypeName);
                } else {
                    return Pair.of(fieldTypeName, right);
                }
            }
            if (resolveClass.isInterface()) {
                right = "{}";
            }
            if (resolveClass.getQualifiedName() != null) {
                newImportList.add(resolveClass.getQualifiedName());
            }
        } else {
            System.out.println(fieldTypeName + " ==> not basic type, not ReferenceType");
        }
        return Pair.of(fieldTypeName, right);
    }

    /**
     * 根据具体集合获取 typeName
     * @param newImportList 引包集合
     * @param project 项目
     * @param typeNameFormat typeName 格式化
     * @param fieldTypeName  类型
     * @param parameters 泛型参数集合
     * @param right
     * @param importCodeStr 需要引入的包
     * @param format 格式化
     * @return 类型
     */
    default Pair<String, String> getFieldTypeNameByCollection(Set<String> newImportList, Project project, String typeNameFormat, String fieldTypeName, PsiType[] parameters, String right, String importCodeStr, String format) {
        if (parameters.length > 0) {
            newImportList.add(importCodeStr);
            PsiType elementType = parameters[0];
            String typeFormat = String.format(typeNameFormat, format);
            String realTypeName = elementType.getPresentableText();
            return handlerByPsiType(newImportList, project, elementType, typeFormat, realTypeName);
        } else {
            return Pair.of(fieldTypeName, right);
        }
    }

    /**
     * 根据 psiType 生成代码
     *
     * @param splitText     分隔符
     * @param newImportList 需要 import 的类
     * @return 生成的代码
     */
    String generateCode(String splitText, HashSet<String> newImportList);

    /**
     * 根据 psiType 生成 code 并插入
     *
     * @param document           文档
     * @param psiDocumentManager 文档管理器
     * @param containingFile     当前文件
     * @param splitText          每行分隔符
     * @param insertOffset       插入的位置
     */
    default void insertCodeByPsiType(Document document, PsiDocumentManager psiDocumentManager, PsiFile containingFile, String splitText, int insertOffset) {
        HashSet<String> newImportList = new HashSet<>();
        String insertCode = generateCode(splitText, newImportList);
        document.insertString(insertOffset, insertCode);
        PsiDocumentUtil.commitAndSaveDocument(psiDocumentManager, document);
        PsiDocumentUtil.addImportToFile(psiDocumentManager, (PsiJavaFile) containingFile, document, newImportList);
    }

    /**
     * 根据 psiType 生成 code 并通过 Template 插入
     *
     * @param document           文档
     * @param psiDocumentManager 文档管理器
     * @param containingFile     当前文件
     * @param editor             editor
     */
    default void insertCodeByPsiTypeWithTemplate(Document document, PsiDocumentManager psiDocumentManager, PsiFile containingFile, Editor editor) {
        insertCodeByPsiTypeWithTemplate(document, psiDocumentManager, containingFile, editor, getTemplateVariables());
    }

    /**
     * 得到模板变量
     * 子类可重写以传递模版变量
     *
     * @return {@link Variable[]}
     */
    @Nullable
    default Variable[] getTemplateVariables() {
        return null;
    }

    /**
     * 插入code通过ψ类型与模板
     * 根据 psiType 生成 code 并通过 Template 插入
     *
     * @param document           文档
     * @param psiDocumentManager 文档管理器
     * @param containingFile     当前文件
     * @param editor             editor
     * @param variableArray      模版变量数组
     */
    default void insertCodeByPsiTypeWithTemplate(Document document, PsiDocumentManager psiDocumentManager, PsiFile containingFile, Editor editor, Variable... variableArray) {
        HashSet<String> newImportList = new HashSet<>();
        String insertCode = generateCode("", newImportList);
        System.out.println("insertCodeByPsiTypeWithTemplate :: " + insertCode);
        TemplateManager manager = TemplateManager.getInstance(containingFile.getProject());
        Template template = manager.createTemplate("", "", insertCode + "$END$");
        if (variableArray != null && variableArray.length > 0) {
            for (Variable variable : variableArray) {
                template.addVariable(variable);
            }
        }
        template.setToReformat(true);
        manager.startTemplate(editor, template);
        PsiDocumentUtil.addImportToFile(psiDocumentManager, (PsiJavaFile) containingFile, document, newImportList);
    }


}
