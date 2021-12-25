package cn.gudqs7.plugins.docer.savior;

import cn.gudqs7.plugins.docer.pojo.FieldExampleInfo;
import cn.gudqs7.plugins.docer.theme.Theme;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiArrayInitializerMemberValue;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.impl.compiled.ClsArrayInitializerMemberValueImpl;
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        Object paramValue;
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
                    content = content.replaceAll("\\{\\{" + key + "}}", value);
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
        int len;
        while ((len = bf.read(buff)) != -1) {
            back.append(new String(buff, 0, len, StandardCharsets.UTF_8));
        }
        bf.close();
        return back.toString();
    }

}
