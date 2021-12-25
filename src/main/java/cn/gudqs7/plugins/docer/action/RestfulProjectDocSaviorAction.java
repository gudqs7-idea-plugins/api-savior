package cn.gudqs7.plugins.docer.action;

import cn.gudqs7.plugins.docer.savior.JavaToDocSavior;
import cn.gudqs7.plugins.docer.theme.ThemeHelper;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;

/**
 * @author wq
 */
public class RestfulProjectDocSaviorAction extends AbstractProjectDocerSavior {

    public RestfulProjectDocSaviorAction() {
        super(new JavaToDocSavior(ThemeHelper.getRestfulTheme()));
    }

    @Override
    protected boolean isNeedDealPsiClass(PsiClass psiClass, Project project) {
        // controller 和 feign 处理, 其他不处理
        PsiAnnotation psiAnnotation = psiClass.getAnnotation("org.springframework.stereotype.Controller");
        if (psiAnnotation == null) {
            psiAnnotation = psiClass.getAnnotation("org.springframework.web.bind.annotation.RestController");
        }
        // 若类不是 Controller 则不显示
        if (psiAnnotation != null) {
            return true;
        }
        return psiClass.getAnnotation("org.springframework.cloud.openfeign.FeignClient") != null;
    }

    @Override
    protected String getFileNamePrefix() {
        return "restful-doc";
    }
}
