package cn.gudqs7.plugins.docer.action.batch;

import cn.gudqs7.plugins.docer.action.base.AbstractBatchDocerSavior;
import cn.gudqs7.plugins.docer.pojo.annotation.CommentInfo;
import cn.gudqs7.plugins.docer.savior.more.JavaToAmpSavior;
import cn.gudqs7.plugins.docer.theme.ThemeHelper;
import cn.gudqs7.plugins.docer.util.ConfigHolder;
import cn.gudqs7.plugins.util.PsiClassUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * 导出相应信息成 AMP 格式 yaml
 * @author wenquan
 * @date 2022/3/30
 */
public class AmpDocerSaviorAction extends AbstractBatchDocerSavior {

    protected JavaToAmpSavior javaToAmpSavior;

    public AmpDocerSaviorAction() {
        this.javaToAmpSavior = new JavaToAmpSavior(ThemeHelper.getRestfulTheme());
    }

    @Override
    protected boolean isNotShow(@NotNull AnActionEvent e, Project project, PsiElement psiElement, PsiClass psiClass, PsiDirectory psiDirectory) {
        boolean flag = true;
        Map<String, String> config = ConfigHolder.getConfig();
        if (config != null) {
            String ampEnable = config.get("amp.enable");
            if ("true".equals(ampEnable)) {
                flag = false;
            }
        }
        return flag;
    }

    @Override
    protected String runLoop0(PsiClass psiClass0, Project project, CommentInfo commentInfo, String moduleName, String fileName, String fullFileName, Map<String, Object> otherMap) {
        return javaToAmpSavior.generateAmpScheme(psiClass0, project);
    }

    @Override
    protected boolean isNeedDealPsiClass(PsiClass psiClass, Project project) {
        return PsiClassUtil.isControllerOrFeign(psiClass);
    }

    @Override
    protected String getDirPrefix() {
        return "amp";
    }

    @Override
    protected @NotNull String getFileExtension() {
        return "yaml";
    }
}
