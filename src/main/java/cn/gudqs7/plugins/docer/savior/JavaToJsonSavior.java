package cn.gudqs7.plugins.docer.savior;

import cn.gudqs7.plugins.docer.constant.MapKeyConstant;
import cn.gudqs7.plugins.docer.pojo.FieldExampleInfo;
import cn.gudqs7.plugins.docer.pojo.JsonExampleInfo;
import cn.gudqs7.plugins.docer.pojo.annotation.ApiModelProperty;
import cn.gudqs7.plugins.docer.pojo.annotation.RequestMapping;
import cn.gudqs7.plugins.docer.theme.Theme;
import cn.gudqs7.plugins.docer.util.AnnotationHolder;
import cn.gudqs7.plugins.docer.util.DataHolder;
import cn.gudqs7.plugins.util.PsiClassUtil;
import cn.gudqs7.plugins.util.PsiUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wq
 */
public class JavaToJsonSavior extends BaseSavior {

    private final ConcurrentHashMap<String, Map<String, Object>> earlyCache = new ConcurrentHashMap<>(16);
    private final ConcurrentHashMap<String, Map<String, Object>> psiClassCache = new ConcurrentHashMap<>(16);

    public JavaToJsonSavior(Theme theme) {
        super(theme);
    }

    public String java2json(PsiClassReferenceType psiClassReferenceType, Project project) {
        if (psiClassReferenceType == null) {
            return "";
        }
        Map<String, Object> json = convertPsiClassToJson(psiClassReferenceType, project);
        psiClassCache.clear();
        earlyCache.clear();
        return theme.formatJson(json, null);
    }

    public Map<String, Object> parameterListOnlyJava2json(PsiParameterList parameterList, Project project) {
        if (parameterList == null || parameterList.isEmpty()) {
            return new HashMap<>();
        }
        Map<String, Object> map = new LinkedHashMap<>();
        String qualifiedName = "__parameters__";
        for (PsiParameter parameter : parameterList.getParameters()) {
            handlePsiParameter(project, map, qualifiedName, parameter);
        }
        psiClassCache.clear();
        earlyCache.clear();
        checkHidden(map, "");
        return map;
    }

    public Map<String, Object> parameterOnlyJava2json(PsiParameter psiParameter, Project project) {
        if (psiParameter == null) {
            return new HashMap<>();
        }
        Map<String, Object> map = new LinkedHashMap<>();
        String qualifiedName = "__parameters__";
        handlePsiParameter(project, map, qualifiedName, psiParameter);
        psiClassCache.clear();
        earlyCache.clear();
        checkHidden(map, "");
        return map;
    }

    private void checkHidden(Map<String, Object> map, String fieldPrefix) {
        if (map == null || map.size() == 0) {
            return;
        }
        String requestBodyKey = null;
        if ("".equals(fieldPrefix)) {
            Object key = map.get(MapKeyConstant.HAS_REQUEST_BODY);
            if (key instanceof String) {
                requestBodyKey = (String) key;
                Object o = map.get(requestBodyKey);
                if (o instanceof Map) {
                    checkHidden((Map<String, Object>) o, "");
                }
            }
        }
        Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            Object key = entry.getKey();
            if (key.equals(requestBodyKey) || key.equals(MapKeyConstant.HAS_REQUEST_BODY)) {
                continue;
            }
            Object value = entry.getValue();
            checkHidden0(iterator, map, fieldPrefix, String.valueOf(key), value);
        }
    }

    private void checkHidden0(Iterator<Map.Entry<String, Object>> iterator, Map<String, Object> parentMap, String fieldPrefix, String key, Object val) {
        if (val == null) {
            return;
        }
        String fieldKey = fieldPrefix + key;
        List<String> hiddenKeys = DataHolder.getData(MapKeyConstant.HIDDEN_KEYS);
        List<String> onlyRequest = DataHolder.getData(MapKeyConstant.ONLY_KEYS);
        if (CollectionUtils.isNotEmpty(hiddenKeys)  ) {
            if (hiddenKeys.contains(fieldKey)) {
                iterator.remove();
                return;
            }
        }
        if (CollectionUtils.isNotEmpty(onlyRequest)) {
            if (!onlyRequest.contains(fieldKey)) {
                iterator.remove();
                return;
            }
        }
        if (val instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) val;
            checkHidden(map, fieldPrefix + key + ".");
        } else if (val instanceof List) {
            List<Object> list = (List<Object>) val;
            for (Object o : list) {
                checkHidden0(iterator, parentMap, fieldPrefix, key, o);
            }
        }
    }

    private void handlePsiParameter(Project project, Map<String, Object> map, String qualifiedName, PsiParameter parameter) {
        String fieldName = parameter.getName();
        PsiType psiFieldType = parameter.getType();
        AnnotationHolder fieldAnnotation = AnnotationHolder.getPsiParameterHolder(parameter);
        boolean notHidden = handlerJsonByFieldInfo(project, qualifiedName, map, fieldName, psiFieldType, fieldAnnotation, null);
        if (notHidden) {
            theme.handleParameterList(parameter, fieldAnnotation, map);
        }
    }

    public String returnOnlyJava2json(PsiMethod publicMethod, PsiType returnType, Project project) {
        if (returnType == null) {
            PsiClass containingClass = publicMethod.getContainingClass();
            if (containingClass == null) {
                PsiUtil.handleSyntaxError(publicMethod.getName()+"'s Class");
            }
            String qualifiedName = containingClass.getQualifiedName();
            PsiUtil.handleSyntaxError(String.format("方法 %s 的返回值不存在?", qualifiedName + "#" + publicMethod.getName()));
        }
        // 普通类型
        String typeName = returnType.getPresentableText();
        if ("void".equals(typeName)) {
            return "无";
        }
        if (isJavaBaseType(typeName)) {
            return String.valueOf(getJavaBaseTypeDefaultValue(typeName, new FieldExampleInfo("{}", "")));
        }
        Object value = getValueByPsiType(returnType, project, new FieldExampleInfo(), null);
        psiClassCache.clear();
        earlyCache.clear();
        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            checkHidden(map, "");
        }
        return theme.formatJson(value, RequestMapping.ContentType.APPLICATION_JSON);
    }

    private Map<String, Object> convertPsiClassToJson(PsiClassReferenceType psiClassReferenceType, Project project) {
        PsiClass psiClass = psiClassReferenceType.resolve();
        if (psiClass == null) {
            PsiUtil.handleSyntaxError(psiClassReferenceType.getCanonicalText());
        }
        String psiClassName = psiClass.getName();
        String qualifiedName = psiClass.getQualifiedName();
        PsiUtil.resolvePsiClassParameter(psiClassReferenceType);
        if (qualifiedName != null) {
            if (psiClassCache.containsKey(qualifiedName)) {
                Map<String, Object> old = psiClassCache.get(qualifiedName);
                return new HashMap<>(old);
            }
            if (earlyCache.containsKey(qualifiedName)) {
                return new HashMap<>(2);
            }
        }
        if (isJavaBaseType(psiClassName) || "Object".equals(psiClassName)) {
            return new HashMap<>(2);
        }
        Map<String, Object> map = new LinkedHashMap<>();
        if (qualifiedName != null) {
            earlyCache.put(qualifiedName, map);
        }
        PsiField[] fields = PsiClassUtil.getAllFieldsByPsiClass(psiClass);
        for (PsiField field : fields) {
            String fieldName = field.getName();
            PsiType psiFieldType = field.getType();
            AnnotationHolder annotationHolder = AnnotationHolder.getPsiFieldHolder(field);
            handlerJsonByFieldInfo(project, qualifiedName, map, fieldName, psiFieldType, annotationHolder, psiClassReferenceType);
        }
        if (qualifiedName != null) {
            psiClassCache.put(qualifiedName, map);
        }
        return map;
    }

    private boolean handlerJsonByFieldInfo(Project project, String qualifiedName, Map<String, Object> map, String fieldName, PsiType psiFieldType, AnnotationHolder annotationHolder, PsiClassReferenceType topClassReferenceType) {
        ApiModelProperty apiModelProperty = annotationHolder.getApiModelProperty();
        String fieldDesc = apiModelProperty.getValue("");
        String example = apiModelProperty.getExample("");
        boolean required = apiModelProperty.isRequired(false);
        FieldExampleInfo fieldExampleInfo = new FieldExampleInfo(fieldDesc, example, required);
        Boolean hidden = apiModelProperty.isHidden(false);
        fieldName = apiModelProperty.getName(fieldName);
        hidden = theme.handleHidden(fieldName, psiFieldType, annotationHolder, hidden);
        if (hidden) {
            return false;
        }
        String canonicalText = psiFieldType.getCanonicalText();
        if (qualifiedName.equals(canonicalText)) {
            map.put(fieldName, new Object());
            return true;
        }
        Object value = getValueByPsiType(psiFieldType, project, fieldExampleInfo, topClassReferenceType);
        map.put(fieldName, value);
        return true;
    }

    public Object getValueByPsiType(PsiType psiFieldType, Project project, FieldExampleInfo memo, PsiClassReferenceType topClassReferenceType) {
        // 普通类型
        String typeName = psiFieldType.getPresentableText();
        if (isJavaBaseType(typeName)) {
            Object realVal = getJavaBaseTypeDefaultValue(typeName, memo);
            return new JsonExampleInfo(realVal, memo);
        }
        String canonicalText = psiFieldType.getCanonicalText();
        boolean isArrayType = psiFieldType instanceof PsiArrayType;
        if (isArrayType) {
            PsiArrayType psiArrayType = (PsiArrayType) psiFieldType;
            PsiType componentType = psiArrayType.getComponentType();
            return getListByType(project, componentType, canonicalText, memo, topClassReferenceType);
        }

        boolean isReferenceType = psiFieldType instanceof PsiClassReferenceType;
        // 引用(枚举/对象/List/Map)
        if (isReferenceType) {
            PsiClassReferenceType psiClassReferenceType = (PsiClassReferenceType) psiFieldType;
            PsiClass resolveClass = psiClassReferenceType.resolve();
            if (resolveClass == null) {
                PsiUtil.handleSyntaxError(psiClassReferenceType.getCanonicalText());
            }

            PsiType realPsiType = PsiUtil.getRealPsiType(psiFieldType, project, null);
            if (realPsiType != null) {
                return getValueByPsiType(realPsiType, project, memo, topClassReferenceType);
            }

            // 枚举
            if (resolveClass.isEnum()) {
                PsiField psiField = resolveClass.getFields()[0];
                return psiField.getName();
            }

            // List
            if (PsiUtil.isPsiTypeFromList(psiFieldType, project)) {
                PsiType[] listParameters = psiClassReferenceType.getParameters();
                if (listParameters.length > 0) {
                    PsiType elementType = listParameters[0];
                    return getListByType(project, elementType, canonicalText, memo, topClassReferenceType);
                } else {
                    List<Object> list = new ArrayList<>(4);
                    list.add(new Object());
                    return list;
                }
            }

            // Map
            if (PsiUtil.isPsiTypeFromMap(psiFieldType, project)) {
                PsiType[] mapParameters = psiClassReferenceType.getParameters();
                if (mapParameters.length > 1) {
                    PsiType keyType = mapParameters[0];
                    PsiType valueType = mapParameters[1];
                    keyType = PsiUtil.getRealPsiType(keyType, project, keyType);
                    String keyTypeName = keyType.getPresentableText();
                    Object key = keyTypeName;
                    if (isJavaBaseType(keyTypeName)) {
                        key = getJavaBaseTypeDefaultValue(keyTypeName, new FieldExampleInfo("key", ""));
                    }
                    Object value = getValueByPsiType(valueType, project, memo, topClassReferenceType);
                    Map<Object, Object> map = new HashMap<>(2);
                    map.put(key, value);
                    return map;
                } else {
                    Map<Object, Object> map = new HashMap<>(2);
                    map.put("key", "value");
                    return map;
                }
            }

            // 普通对象
            return convertPsiClassToJson(psiClassReferenceType, project);
        } else {
            System.out.println(psiFieldType.getPresentableText() + " ==> not basic type, not ReferenceType");
        }
        return new Object();
    }

    @NotNull
    private List<Object> getListByType(Project project, PsiType elementType, String canonicalText, FieldExampleInfo memo, PsiClassReferenceType topClassReferenceType) {
        String elementTypeCanonicalText = elementType.getCanonicalText();
        Object obj;
        if (elementTypeCanonicalText.equals(canonicalText)) {
            // 自己持有自己.....
            obj = new Object();
        } else {
            obj = getValueByPsiType(elementType, project, memo, topClassReferenceType);
        }
        List<Object> list = new ArrayList<>(4);
        list.add(obj);
        return list;
    }

}
