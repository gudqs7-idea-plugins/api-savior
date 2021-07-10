package cn.gudqs7.plugins.idea.action;

import cn.gudqs7.plugins.idea.savior.JavaToDocSavior;
import cn.gudqs7.plugins.idea.theme.ThemeHelper;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;

/**
 * @author wq
 */
public class HsfProjectDocSaviorAction extends AbstractProjectDocerSavior {

    public HsfProjectDocSaviorAction() {
        super(new JavaToDocSavior(ThemeHelper.getHsfTheme()));
    }

    @Override
    protected boolean isNeedDealPsiClass(PsiClass psiClass, Project project) {
        if (psiClass.isInterface()) {
            String name = psiClass.getQualifiedName();
            String qNameOflist = "java.util.List";
            String qNameOfMap = "java.util.Map";
            String qNameOfSet = "java.util.Set";
            boolean psiClassFromList = docSavior.isPsiClassFromXxx(psiClass, project, qNameOflist);
            boolean psiClassFromMap = docSavior.isPsiClassFromXxx(psiClass, project, qNameOfMap);
            boolean psiClassFromSet = docSavior.isPsiClassFromXxx(psiClass, project, qNameOfSet);
            if (psiClassFromList
                    || psiClassFromMap
                    || psiClassFromSet
            ) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    protected String getFileNamePrefix() {
        return "hsf-doc";
    }
}
