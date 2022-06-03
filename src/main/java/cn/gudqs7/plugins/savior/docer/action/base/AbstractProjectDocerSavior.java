package cn.gudqs7.plugins.savior.docer.action.base;

import cn.gudqs7.plugins.savior.docer.pojo.annotation.CommentInfo;
import cn.gudqs7.plugins.savior.docer.savior.more.JavaToDocSavior;
import cn.gudqs7.plugins.savior.docer.util.ActionUtil;
import cn.gudqs7.plugins.savior.docer.util.ClipboardUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;

import java.util.Map;
import java.util.Set;

/**
 * @author wq
 */
public abstract class AbstractProjectDocerSavior extends AbstractBatchDocerSavior {

    protected JavaToDocSavior docSavior;

    public AbstractProjectDocerSavior(JavaToDocSavior docSavior) {
        this.docSavior = docSavior;
    }

    @Override
    protected String runLoop0(PsiClass psiClass0, Project project, CommentInfo commentInfo, String moduleName, String fileName, String fullFileName, Map<String, Object> otherMap) {
        return docSavior.generateApiByServiceInterface(psiClass0, project);
    }

    @Override
    protected boolean handleRightClickOnClass(Project project, PsiClass psiClass, Set<PsiClass> psiClassList) {
        if (isNeedDealPsiClass(psiClass, project)) {
            String docByInterface = docSavior.generateApiByServiceInterface(psiClass, project);
            ClipboardUtil.setSysClipboardText(docByInterface);
            ActionUtil.showDialog(project, "已自动的将 Markdown 文档复制到您的剪切板!\n您可在此预览后再去粘贴!", docByInterface);
            return true;
        }
        return false;
    }

}
