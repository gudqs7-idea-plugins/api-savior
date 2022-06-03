package cn.gudqs7.plugins.savior.docer.savior.base;

import cn.gudqs7.plugins.savior.docer.theme.Theme;
import cn.gudqs7.plugins.savior.util.PsiAnnotationUtil;
import cn.gudqs7.plugins.savior.util.StringUtil;
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
        return StringUtil.replaceMd(source);
    }

}
