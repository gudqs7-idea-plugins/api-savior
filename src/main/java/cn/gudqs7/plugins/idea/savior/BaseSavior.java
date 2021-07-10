package cn.gudqs7.plugins.idea.savior;

import cn.gudqs7.plugins.idea.pojo.FieldExampleInfo;
import cn.gudqs7.plugins.idea.theme.Theme;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.compiled.ClsArrayInitializerMemberValueImpl;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl;
import com.intellij.psi.search.GlobalSearchScope;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.*;

/**
 * @author wq
 */
public abstract class BaseSavior {

    protected Theme theme;

    public Theme getTheme() {
        return theme;
    }

    public BaseSavior(Theme theme) {
        this.theme = theme;
    }

    // =============== clipboard util ===============

    /**
     * 从剪切板获得文字。
     */
    public String getSysClipboardText() {
        String ret = "";
        Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();
        // 获取剪切板中的内容
        Transferable clipTf = sysClip.getContents(null);

        if (clipTf != null) {
            // 检查内容是否是文本类型
            if (clipTf.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                try {
                    ret = (String) clipTf
                            .getTransferData(DataFlavor.stringFlavor);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return ret;
    }

    /**
     * 将字符串复制到剪切板。
     */
    public void setSysClipboardText(String writeMe) {
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable tText = new StringSelection(writeMe);
        clip.setContents(tText, null);
    }


    // =============== annotation util ===============

    public static <T> T getAnnotationValue(PsiAnnotation fieldAnnotation, String attr, T defaultVal) {
        PsiAnnotationMemberValue value = fieldAnnotation.findAttributeValue(attr);
        if (value == null) {
            return defaultVal;
        }
        Object valueByPsiAnnotationMemberValue = getValueByPsiAnnotationMemberValue(value);
        if (valueByPsiAnnotationMemberValue != null) {
            return (T) valueByPsiAnnotationMemberValue;
        }
        return defaultVal;
    }

    public static <T> T getValueByPsiAnnotationMemberValue(PsiAnnotationMemberValue value) {
        if (value instanceof ClsArrayInitializerMemberValueImpl) {
            ClsArrayInitializerMemberValueImpl clsArrayInitializerMemberValue = (ClsArrayInitializerMemberValueImpl) value;
            return (T) clsArrayInitializerMemberValue.getText();
        }
        if (value instanceof PsiLiteralExpressionImpl) {
            PsiLiteralExpressionImpl expression = (PsiLiteralExpressionImpl) value;
            Object expressionValue = expression.getValue();
            if (expressionValue != null) {
                return (T) expressionValue;
            }
        }
        if (value instanceof PsiReferenceExpression) {
            PsiReferenceExpression psiReferenceExpression = (PsiReferenceExpression) value;
            String text = psiReferenceExpression.getText();
            String prefixWithRequestMethod = "RequestMethod.";
            if (text.startsWith(prefixWithRequestMethod)) {
                return (T) text.substring(prefixWithRequestMethod.length());
            } else {
                return (T) text;
            }
        }
        if (value instanceof PsiArrayInitializerMemberValue) {
            PsiArrayInitializerMemberValue psiArrayInitializerMemberValue = (PsiArrayInitializerMemberValue) value;
            PsiAnnotationMemberValue[] memberValues = psiArrayInitializerMemberValue.getInitializers();
            if (memberValues != null && memberValues.length > 0) {
                StringBuilder all = new StringBuilder();
                List list = new ArrayList();
                for (PsiAnnotationMemberValue memberValue : memberValues) {
                    Object item = getValueByPsiAnnotationMemberValue(memberValue);
                    if (item == null) {
                        list.add(memberValue);
                    }
                    all.append(String.valueOf(item)).append("/");
                }
                if (list.size() > 0) {
                    return (T) list;
                }
                return (T) all.substring(0, all.length() - 1);
            }
        }
        return null;
    }


    // =============== psi class util ===============

    public PsiClassReferenceType getPsiClassByPsiType(PsiType psiType) {
        if (psiType != null) {
            if (psiType instanceof PsiClassReferenceType) {
                return (PsiClassReferenceType) psiType;
            }
        }
        return null;
    }

    public PsiClass findOnePsiClassByClassName(String qualifiedClassName, Project project) {
        PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(qualifiedClassName, GlobalSearchScope.allScope(project));
        return psiClass;
    }

    protected void handleSyntaxError(String code) throws RuntimeException {
        throw new RuntimeException("您的代码可能存在语法错误, 无法为您生成代码, 参考信息: " + code);
    }

    @NotNull
    protected PsiMethod[] getAllMethods(PsiClass psiClass) {
        PsiMethod[] methods = psiClass.getMethods();
        PsiClassType[] extendsListTypes = psiClass.getExtendsListTypes();
        if (extendsListTypes == null || extendsListTypes.length == 0) {
            return methods;
        }

        List<PsiMethod> allMethods = new ArrayList<PsiMethod>();
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

    protected PsiField[] getAllFieldsByPsiClass(PsiClass psiClass) {
        PsiField[] fields = psiClass.getFields();
        PsiClassType[] extendsListTypes = psiClass.getExtendsListTypes();
        if (extendsListTypes == null || extendsListTypes.length == 0) {
            return fields;
        }
        List<PsiField> allFields = new ArrayList<PsiField>();
        for (PsiClassType extendsListType : extendsListTypes) {
            PsiClass extendCls = extendsListType.resolve();
            allFields.addAll(Arrays.asList(getAllFieldsByPsiClass(extendCls)));
        }
        allFields.addAll(Arrays.asList(fields));
        return allFields.toArray(new PsiField[0]);
    }

    // =============== psi generic util ===============

    public Map<String, PsiType[]> genericListMap = new HashMap<>();

    protected void resolvePsiClassParameter(PsiClassType psiClassReferenceType) {
        PsiClass psiClass = psiClassReferenceType.resolve();
        String qualifiedName = psiClass.getQualifiedName();
        PsiClassType[] extendsListTypes = psiClass.getExtendsListTypes();
        PsiType[] parameters = psiClassReferenceType.getParameters();
        if (parameters.length > 0) {
            genericListMap.put(qualifiedName, parameters);
        }
        if (extendsListTypes != null && extendsListTypes.length > 0) {
            for (PsiClassType extendsListType : extendsListTypes) {
                resolvePsiClassParameter(extendsListType);
            }
        }
    }

    private PsiType getRealPsiType0(String ownerQname, int index, Project project, PsiType defaultVal) {
        PsiType[] psiTypes = genericListMap.get(ownerQname);
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
        }
        return defaultVal;
    }

    protected PsiType getRealPsiType(PsiType psiFieldType, Project project, PsiType defaultVal) {
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

    protected String getRealPsiTypeName(PsiType psiType, Project project, String typeNameFormat) {
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
            if (isPsiTypeFromList(psiType, project)) {
                if (parameters.length > 0) {
                    PsiType elementType = parameters[0];
                    String typeFormat = String.format(typeNameFormat, "List<%s>");
                    return getRealPsiTypeName(elementType, project, typeFormat);
                }
            }
            if (isPsiTypeFromMap(psiType, project)) {
                if (parameters.length > 1) {
                    PsiType keyType = parameters[0];
                    PsiType valueType = parameters[1];
                    String keyTypeName = "String";
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

    protected boolean isPsiTypeFromParameter(PsiType psiFieldType) {
        if (psiFieldType instanceof PsiClassReferenceType) {
            PsiClassReferenceType psiClassReferenceType = (PsiClassReferenceType) psiFieldType;
            PsiClass resolveClass = psiClassReferenceType.resolve();
            if (resolveClass == null) {
                handleSyntaxError(psiClassReferenceType.getCanonicalText());
            }
            if (resolveClass instanceof PsiTypeParameter) {
                return true;
            }
        }
        return false;
    }

    protected boolean isPsiTypeFromList(PsiType psiFieldType, Project project) {
        boolean isReferenceType = psiFieldType instanceof PsiClassReferenceType;
        if (isReferenceType) {
            PsiClassReferenceType psiClassReferenceType = (PsiClassReferenceType) psiFieldType;
            PsiClass resolveClass = psiClassReferenceType.resolve();
            if (resolveClass == null) {
                handleSyntaxError(psiClassReferenceType.getCanonicalText());
            }
            String qNameOfList = "java.util.List";
            return isPsiClassFromXxx(resolveClass, project, qNameOfList);
        }
        return false;
    }

    protected boolean isPsiTypeFromMap(PsiType psiFieldType, Project project) {
        boolean isReferenceType = psiFieldType instanceof PsiClassReferenceType;
        if (isReferenceType) {
            PsiClassReferenceType psiClassReferenceType = (PsiClassReferenceType) psiFieldType;
            PsiClass resolveClass = psiClassReferenceType.resolve();
            if (resolveClass == null) {
                handleSyntaxError(psiClassReferenceType.getCanonicalText());
            }
            String qNameOfMap = "java.util.Map";
            return isPsiClassFromXxx(resolveClass, project, qNameOfMap);
        }
        return false;
    }

    public boolean isPsiClassFromXxx(PsiClass psiClass, Project project, String qNameOfXxx) {
        String qNameOfClass = psiClass.getQualifiedName();
        if (StringUtils.isBlank(qNameOfClass)) {
            return false;
        }
        PsiClassType psiType = PsiType.getTypeByName(qNameOfClass, project, GlobalSearchScope.allScope(project));
        PsiClassType xxxType = PsiType.getTypeByName(qNameOfXxx, project, GlobalSearchScope.allScope(project));
        boolean assignableFromXxx = xxxType.isAssignableFrom(psiType);
        PsiClass xxxClass = findOnePsiClassByClassName(qNameOfXxx, project);
        boolean isXxxType = psiClass.isInheritor(xxxClass, true);
        if (assignableFromXxx || isXxxType) {
            return true;
        }
        return false;
    }

    // ===============  markdown util ===============

    public static String replaceMd(String source) {
        source = source.replaceAll("\\$", "\\\\\\$");
        source = source.replaceAll("\\{", "\\\\{");
        source = source.replaceAll("\\}", "\\\\}");
        source = source.replaceAll("\\<", "\\\\<");
        source = source.replaceAll("\\>", "\\\\>");
        source = source.replaceAll("\\|", "\\\\|");
        source = source.replaceAll("&br;", "<br>");
        return source;
    }

    // =============== base type util ===============

    public static boolean isJavaBaseType(String typeName) {
        return getJavaBaseTypeDefaultValue(typeName, "") != null;
    }

    public static Object getJavaBaseTypeDefaultValue(String paramType, String example) {
        return getJavaBaseTypeDefaultValue(paramType, new FieldExampleInfo("", example));
    }

    public static Object getJavaBaseTypeDefaultValue(String paramType, @NotNull FieldExampleInfo fieldExampleInfo) {
        String example = fieldExampleInfo.getExample();
        String fieldDesc = fieldExampleInfo.getFieldDesc();
        Object paramValue = null;
        boolean noExampleValue = StringUtils.isBlank(example);
        if (noExampleValue) {
            example = "0";
        }
        try {
            switch (paramType.toLowerCase()) {
                case "byte":
                    paramValue = Byte.parseByte(example);
                    break;
                case "char":
                case "character":
                    paramValue = example.charAt(0);
                    break;
                case "boolean":
                    paramValue = Boolean.parseBoolean(example);
                    break;
                case "int":
                case "integer":
                    paramValue = Integer.parseInt(example);
                    break;
                case "double":
                    paramValue = Double.parseDouble(example);
                    break;
                case "float":
                    paramValue = Float.parseFloat(example);
                    break;
                case "long":
                    paramValue = Long.parseLong(example);
                    break;
                case "short":
                    paramValue = Short.parseShort(example);
                    break;
                case "number":
                    paramValue = 0;
                    break;
                case "bigdecimal":
                    paramValue = new BigDecimal(example);
                    break;
                case "string":
                case "date":
                    if (noExampleValue) {
                        paramValue = fieldDesc;
                    } else {
                        paramValue = example;
                    }
                    break;
                default:
                    paramValue = null;
            }
        } catch (Exception e) {
            paramValue = example;
        }
        return paramValue;
    }


    // =============== template util ===============

    public static String getTemplate(String path, Map<String, String> data) {
        try {
            String content = readFile(JavaToDocSavior.class.getClassLoader().getResourceAsStream(path));
            if (data != null) {
                for (String key : data.keySet()) {
                    String value = data.get(key);
                    value = replaceMd(value);
                    content = content.replaceAll("\\{\\{" + key + "\\}\\}", value);
                }
            }
            return content;
        } catch (Exception e) {
            throw new RuntimeException("getTemplate has error: " + e);
        }
    }

    private static String readFile(InputStream resourceAsStream) throws IOException {
        StringBuilder back = new StringBuilder();
        BufferedInputStream bf = new BufferedInputStream(resourceAsStream);
        byte[] buff = new byte[4096];
        int len = -1;
        while ((len = bf.read(buff)) != -1) {
            back.append(new String(buff, 0, len, "utf-8"));
        }
        bf.close();
        return back.toString();
    }

}
