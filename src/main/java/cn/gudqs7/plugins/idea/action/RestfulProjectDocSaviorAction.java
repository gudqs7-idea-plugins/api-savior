package cn.gudqs7.plugins.idea.action;

import cn.gudqs7.plugins.idea.savior.JavaToDocSavior;
import cn.gudqs7.plugins.idea.theme.ThemeHelper;
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
        boolean isFeignClient = psiClass.getAnnotation("org.springframework.cloud.openfeign.FeignClient") != null;
        if (isFeignClient) {
            return true;
        }

        return false;
    }

    @Override
    protected String getFileNamePrefix() {
        return "restful-doc";
    }
}
