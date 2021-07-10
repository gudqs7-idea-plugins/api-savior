package cn.gudqs7.plugins.idea.savior;

import cn.gudqs7.plugins.idea.constant.MapKeyConstant;
import cn.gudqs7.plugins.idea.pojo.ParamInfo;
import cn.gudqs7.plugins.idea.pojo.ParamLineInfo;
import cn.gudqs7.plugins.idea.pojo.annotation.ApiModelProperty;
import cn.gudqs7.plugins.idea.theme.Theme;
import cn.gudqs7.plugins.idea.util.AnnotationHolder;
import cn.gudqs7.plugins.idea.util.DataHolder;
import cn.gudqs7.plugins.idea.util.IndexIncrementUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wq
 */
public class JavaToApiSavior extends BaseSavior {

    private ConcurrentHashMap<String, Map<String, Object>> earlyCache = new ConcurrentHashMap<>(16);
    private ConcurrentHashMap<String, Map<String, Object>> psiClassCache = new ConcurrentHashMap<>(16);

    public JavaToApiSavior(Theme theme) {
        super(theme);
    }

    public String java2api(PsiClassReferenceType psiClassReferenceType, Project project) {
        if (psiClassReferenceType == null) {
            return "";
        }
        PsiClass psiClass = psiClassReferenceType.resolve();
        Map<Integer, List<ParamInfo>> goMap = new TreeMap<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {

                return o1 - o2;
            }
        });
        String clazzDesc = "未知";
        AnnotationHolder annotationHolder = AnnotationHolder.getPsiClassHolder(psiClass);
        ApiModelProperty apiModelProperty = annotationHolder.getApiModelProperty();
        clazzDesc = apiModelProperty.getValue(clazzDesc);

        convertPsiClassToMarkdown(project, goMap, psiClassReferenceType, clazzDesc, 1, null, "");

        String allStr = printByGoMap(goMap, false);
        psiClassCache.clear();
        earlyCache.clear();
        return allStr;
    }

    public String parameterListOnlyJava2api(PsiParameterList parameterList, Project project) {
        if (parameterList == null || parameterList.isEmpty()) {
            return "";
        }
        Map<Integer, List<ParamInfo>> goMap = new TreeMap<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }
        });
        convertPsiParameterListToMarkdown(project, goMap, parameterList);

        String allStr = printByGoMap(goMap, false);
        psiClassCache.clear();
        earlyCache.clear();
        return allStr;
    }

    public String returnOnlyJava2api(PsiTypeElement returnTypeElement, Project project) {
        if (returnTypeElement == null) {
            return "";
        }
        PsiType returnType = returnTypeElement.getType();
        if ("void".equals(returnType.getPresentableText())) {
            return theme.handleNoField(true);
        }
        String clazzTypeName = "响应结果";
        return java2apiCommon(project, returnType, "", clazzTypeName, AnnotationHolder.getPsiReturnTypeHolder(returnTypeElement), true);
    }

    public String parameterOnlyJava2api(PsiParameter parameter, Project project) {
        if (parameter == null) {
            return "";
        }
        PsiType psiType = parameter.getType();
        String fieldName = parameter.getName();

        String clazzTypeName = "请求参数";
        return java2apiCommon(project, psiType, fieldName, clazzTypeName, AnnotationHolder.getPsiParameterHolder(parameter), false);
    }

    @NotNull
    private String java2apiCommon(Project project, PsiType psiType, String fieldName, String clazzTypeName, AnnotationHolder fieldAnnotation, boolean returnParam) {
        Map<Integer, List<ParamInfo>> goMap = new TreeMap<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }
        });

        List<ParamLineInfo> levelOther = new ArrayList<>();
        int level = 0;
        // 普通类型
        String typeName = psiType.getPresentableText();
        if (isJavaBaseType(typeName) || "Object".equals(typeName) || isPsiTypeFromMap(psiType, project)) {
            level = 1;
        }
        handlerByFieldInfo(project, goMap, level, clazzTypeName, levelOther, fieldName, psiType, fieldAnnotation, null, returnParam, MapKeyConstant.FIELD_PREFIX_INIT);

        dealGoMap(goMap, "", level, "", clazzTypeName, levelOther);
        String allStr = printByGoMap(goMap, returnParam);
        psiClassCache.clear();
        earlyCache.clear();
        return allStr;
    }

    private void convertPsiParameterListToMarkdown(Project project, Map<Integer, List<ParamInfo>> goMap, PsiParameterList parameterList) {
        String clazzTypeName = "Params";
        String clazzDesc = "接口参数列表";
        PsiParameter[] typeDeclaredFields = parameterList.getParameters();
        List<ParamLineInfo> levelOther = new ArrayList<>();
        for (PsiParameter psiParameter : typeDeclaredFields) {
            String fieldName = psiParameter.getName();
            PsiType psiFieldType = psiParameter.getType();
            AnnotationHolder annotationHolder = AnnotationHolder.getPsiParameterHolder(psiParameter);

            handlerByFieldInfo(project, goMap, 1, clazzTypeName, levelOther, fieldName, psiFieldType, annotationHolder, null, false, MapKeyConstant.FIELD_PREFIX_INIT);
        }
        dealGoMap(goMap, clazzDesc, 1, null, clazzTypeName, levelOther);
    }

    private void convertPsiClassToMarkdown(Project project, Map<Integer, List<ParamInfo>> goMap, PsiClassReferenceType psiClassReferenceType, String clazzDesc, int level, String parentClazzTypeName, String fieldPrefix) {
        if (psiClassReferenceType == null) {
            return;
        }
        PsiClass psiClass = psiClassReferenceType.resolve();
        String clazzTypeName = psiClass.getName();
        String qualifiedName = psiClass.getQualifiedName();
        resolvePsiClassParameter(psiClassReferenceType);
        if (qualifiedName != null) {
            if (psiClassCache.containsKey(qualifiedName)) {
                return;
            }
            if (earlyCache.containsKey(qualifiedName)) {
                return;
            }
        }
        if (isJavaBaseType(clazzTypeName) || "Object".equals(clazzTypeName)) {
            return;
        }
        if (qualifiedName != null) {
            earlyCache.put(qualifiedName, new HashMap<>());
        }
        PsiField[] typeDeclaredFields = getAllFieldsByPsiClass(psiClass);
        List<ParamLineInfo> levelOther = new ArrayList<>();
        for (PsiField typeDeclaredField : typeDeclaredFields) {
            String fieldName = typeDeclaredField.getName();
            PsiType psiFieldType = typeDeclaredField.getType();
            AnnotationHolder annotationHolder = AnnotationHolder.getPsiFieldHolder(typeDeclaredField);
            ApiModelProperty apiModelProperty = annotationHolder.getApiModelProperty();
            fieldName = apiModelProperty.getName(fieldName);
            if (checkHiddenRequest(fieldPrefix, fieldName)) {
                continue;
            }

            handlerByFieldInfo(project, goMap, level, clazzTypeName, levelOther, fieldName, psiFieldType, annotationHolder, psiClassReferenceType, false, fieldPrefix);

        }
        dealGoMap(goMap, clazzDesc, level, parentClazzTypeName, clazzTypeName, levelOther);
        if (qualifiedName != null) {
            psiClassCache.put(qualifiedName, new HashMap<>());
        }
    }

    private boolean checkHiddenRequest(String fieldPrefix, String fieldName) {
        String fieldKey = fieldPrefix + fieldName;
        List<String> hiddenRequest = DataHolder.getData(MapKeyConstant.HIDDEN_KEYS);
        if (hiddenRequest != null) {
            return hiddenRequest.contains(fieldKey);
        }
        return false;
    }

    @NotNull
    private String printByGoMap(Map<Integer, List<ParamInfo>> goMap, boolean returnParam) {
        return theme.printByGoMap(goMap, returnParam);
    }

    private void dealGoMap(Map<Integer, List<ParamInfo>> goMap, String clazzDesc, int level, String parentClazzTypeName, String clazzTypeName, List<ParamLineInfo> levelOther) {
        boolean afterLevelTwo = level > 2;
        if (afterLevelTwo && parentClazzTypeName != null) {
            clazzTypeName = parentClazzTypeName + "=>" + clazzTypeName;
        }
        String addition = "";
        if (StringUtils.isNoneBlank(clazzDesc)) {
            addition = "（" + clazzDesc + "）";
        }

        ParamInfo paramInfo = new ParamInfo();
        int index = IndexIncrementUtil.getIndex();
        paramInfo.setLevel(level);
        paramInfo.setIndex(index);
        paramInfo.setEn(clazzTypeName);
        paramInfo.setCn(addition);
        paramInfo.setAllFields(levelOther);

        List<ParamInfo> list = goMap.get(level);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(paramInfo);
        goMap.put(level, list);
    }

    private void handlerByFieldInfo(Project project, Map<Integer, List<ParamInfo>> goMap, int level, String clazzTypeName, List<ParamLineInfo> levelOther, String fieldName, PsiType psiFieldType, AnnotationHolder annotationHolder, PsiClassReferenceType topClassReferenceType, boolean returnParam, String fieldPrefix) {
        if (topClassReferenceType != null) {
            String qualifiedName = topClassReferenceType.resolve().getQualifiedName();
            String canonicalText = psiFieldType.getCanonicalText();
            if (qualifiedName.equals(canonicalText)) {
                // 自己依赖自己
                return;
            }
        }
        String typeName = psiFieldType.getPresentableText();
        Map<String, String> data = handlerByPsiType(project, goMap, level, fieldName, psiFieldType,
                annotationHolder, clazzTypeName, "%s", typeName, fieldPrefix);
        if (data != null) {
            String result = getTemplate(theme.getParamContentPath(returnParam), data);
            int index = Integer.parseInt(data.getOrDefault("index", "0"));
            ParamLineInfo paramLineInfo = new ParamLineInfo(index, result, level);
            levelOther.add(paramLineInfo);
        }
    }

    private Map<String, String> handlerByPsiType(Project project, Map<Integer, List<ParamInfo>> goMap, int level, String fieldName, PsiType psiFieldType, AnnotationHolder annotationHolder, String clazzTypeName, String typeNameFormat, String typeName, String fieldPrefix) {
        ApiModelProperty apiModelProperty = annotationHolder.getApiModelProperty();
        boolean hidden = apiModelProperty.isHidden(false);
        boolean required = apiModelProperty.isRequired(false);
        String fieldDesc = apiModelProperty.getValue("");
        String notes = apiModelProperty.getNotes("");
        fieldName = apiModelProperty.getName(fieldName);
        hidden = theme.handleHidden(fieldName, psiFieldType, annotationHolder, hidden);
        if (hidden) {
            return null;
        }
        if (StringUtils.isNotBlank(fieldDesc)) {
            fieldDesc = replaceMd(fieldDesc);
        }
        if (StringUtils.isNotBlank(notes)) {
            notes = replaceMd(notes);
        }
        String fieldTypeName = String.format(typeNameFormat, typeName);
        if (StringUtils.isNotBlank(fieldTypeName)) {
            fieldTypeName = replaceMd(fieldTypeName);
        }
        String requiredStr = required ? "是" : "否";
        String requiredStrMarkdown = required ? "**是**" : "否";
        Map<String, String> data = new HashMap<>(16);
        data.put("fieldName", fieldName);
        data.put("fieldType", fieldTypeName);
        data.put("requiredStr", requiredStr);
        data.put("requiredStrMarkdown", requiredStrMarkdown);
        data.put("fieldDesc", fieldDesc);
        data.put("notes", notes);
        data.put("levelPrefix", getLevelStr(level));
        data.put("index", String.valueOf(IndexIncrementUtil.getIndex()));

        String typeSimpleName = psiFieldType.getPresentableText();
        if (isJavaBaseType(typeSimpleName) || "Object".equals(typeSimpleName)) {
            return data;
        }

        boolean isArrayType = psiFieldType instanceof PsiArrayType;
        if (isArrayType) {
            PsiArrayType psiArrayType = (PsiArrayType) psiFieldType;
            PsiType componentType = psiArrayType.getComponentType();
            String typeFormat = String.format(typeNameFormat, "%s[]");
            String realTypeName = componentType.getPresentableText();
            return handlerByPsiType(project, goMap, level, fieldName, componentType, annotationHolder, clazzTypeName, typeFormat, realTypeName, fieldPrefix);
        }

        boolean isReferenceType = psiFieldType instanceof PsiClassReferenceType;
        // 引用(枚举/对象/List/Map)
        if (isReferenceType) {
            PsiClassReferenceType psiClassReferenceType = (PsiClassReferenceType) psiFieldType;
            PsiClass resolveClass = psiClassReferenceType.resolve();
            if (resolveClass == null) {
                handleSyntaxError(psiClassReferenceType.getCanonicalText());
            }

            PsiType[] parameters = psiClassReferenceType.getParameters();
            if (parameters.length > 0) {
                StringBuilder name = new StringBuilder();
                for (PsiType parameter : parameters) {
                    String realTypeName = getRealPsiTypeName(parameter, project, "%s");
                    name.append(realTypeName).append(", ");
                }
                if (name.length() != 0) {
                    String name0 = name.substring(0, name.length() - 2);
                    typeSimpleName = typeSimpleName.substring(0, typeSimpleName.indexOf("<")) + "<" + name0 + ">";
                }
                // 此处 put 只影响普通 class, 不影响 list, map 等
                typeSimpleName = replaceMd(typeSimpleName);
                data.put("fieldType", typeSimpleName);
            }


            PsiType realPsiType = getRealPsiType(psiFieldType, project, null);
            if (realPsiType != null) {
                String realTypeName = realPsiType.getPresentableText();
                String typeFormat = String.format(typeNameFormat, "%s");
                return handlerByPsiType(project, goMap, level, fieldName, realPsiType, annotationHolder, clazzTypeName, typeFormat, realTypeName, fieldPrefix);
            }

            // 枚举
            if (resolveClass.isEnum()) {
                return data;
            }

            // List
            if (isPsiTypeFromList(psiFieldType, project)) {
                if (parameters != null && parameters.length > 0) {
                    PsiType elementType = parameters[0];
                    String typeFormat = String.format(typeNameFormat, "List<%s>");
                    String realTypeName = elementType.getPresentableText();
                    return handlerByPsiType(project, goMap, level, fieldName, elementType, annotationHolder, clazzTypeName, typeFormat, realTypeName, fieldPrefix);
                } else {
                    return data;
                }
            }

            // Map
            if (isPsiTypeFromMap(psiFieldType, project)) {
                if (parameters != null && parameters.length > 1) {
                    PsiType keyType = parameters[0];
                    PsiType valueType = parameters[1];
                    String keyTypeName = "String";
                    if (isPsiTypeFromParameter(keyType)) {
                        keyType = getRealPsiType(keyType, project, keyType);
                    }
                    keyTypeName = keyType.getPresentableText();
                    String typeFormat = String.format(typeNameFormat, "Map<" + keyTypeName + ", %s>");
                    String realTypeName = valueType.getPresentableText();
                    return handlerByPsiType(project, goMap, level, fieldName, valueType, annotationHolder, clazzTypeName, typeFormat, realTypeName, fieldPrefix);
                } else {
                    return data;
                }
            }

            if (!isPsiTypeFromParameter(psiFieldType)) {
                // 普通对象
                String fieldPrefix0 = fieldPrefix + fieldName + ".";
                if (MapKeyConstant.FIELD_PREFIX_INIT.equals(fieldPrefix)) {
                    fieldPrefix0 = "";
                }
                convertPsiClassToMarkdown(project, goMap, psiClassReferenceType, fieldDesc, level + 1, clazzTypeName, fieldPrefix0);
            }

        } else {
            System.out.println(psiFieldType.getPresentableText() + " ==> not basic type, not ReferenceType");
        }
        return data;
    }

    private String getLevelStr(int level) {
        if (level < 2) {
            return "";
        } else {
            StringBuilder str = new StringBuilder("└─");
            for (int i = 2; i < level; i++) {
                str.insert(0, "&ensp;&ensp;&ensp;&ensp;");
            }
            return str.toString();
        }
    }

}
