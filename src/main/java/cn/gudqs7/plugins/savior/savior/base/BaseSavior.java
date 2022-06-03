package cn.gudqs7.plugins.savior.savior.base;

import cn.gudqs7.plugins.common.util.PsiAnnotationUtil;
import cn.gudqs7.plugins.common.util.StringTool;
import cn.gudqs7.plugins.savior.theme.Theme;
import com.intellij.psi.PsiAnnotation;

import java.util.List;

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

    protected <T> List<T> getAnnotationListValue(PsiAnnotation fieldAnnotation, String attr, T defaultVal) {
        return PsiAnnotationUtil.getAnnotationListValue(fieldAnnotation, attr, defaultVal);
    }

    protected <T> T getAnnotationValue(PsiAnnotation fieldAnnotation, String attr, T defaultVal) {
        return PsiAnnotationUtil.getAnnotationValue(fieldAnnotation, attr, defaultVal);
    }

    // ===============  markdown util ===============

    protected String replaceMd(String source) {
        return StringTool.replaceMd(source);
    }

}
