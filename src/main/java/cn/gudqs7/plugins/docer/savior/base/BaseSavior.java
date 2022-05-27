package cn.gudqs7.plugins.docer.savior.base;

import cn.gudqs7.plugins.docer.constant.CommentConst;
import cn.gudqs7.plugins.docer.savior.more.JavaToDocSavior;
import cn.gudqs7.plugins.docer.theme.Theme;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiArrayInitializerMemberValue;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.impl.compiled.ClsArrayInitializerMemberValueImpl;
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
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
            return (T) new ArrayList<>();
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
            if (memberValues.length > 0) {
                List<Object> list = new ArrayList<>();
                for (PsiAnnotationMemberValue memberValue : memberValues) {
                    Object item = getValueByPsiAnnotationMemberValue(memberValue);
                    if (item == null) {
                        list.add(memberValue);
                    } else {
                        list.add(item);
                    }
                }
                if (list.size() > 0) {
                    return (T) list;
                }
            }
        }
        return null;
    }

    // ===============  markdown util ===============

    public static String replaceMd(String source) {
        if (source == null) {
            return null;
        }
        source = source.replaceAll("\\$", "\\\\\\$");
        source = source.replaceAll("\\{", "\\\\{");
        source = source.replaceAll("\\}", "\\\\}");
        source = source.replaceAll("\\<", "\\\\<");
        source = source.replaceAll("\\>", "\\\\>");
        source = source.replaceAll("\\|", "\\\\|");
        source = source.replaceAll(CommentConst.BREAK_LINE, "<br>");
        return source;
    }

    // =============== template util ===============

    public static String getTemplate(String path, Map<String, ?> data) {
        try {
            String content = readFile(JavaToDocSavior.class.getClassLoader().getResourceAsStream(path));
            if (data != null) {
                for (String key : data.keySet()) {
                    String value = String.valueOf(data.get(key));
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
