package cn.gudqs7.plugins.docer.action;

import cn.gudqs7.plugins.docer.savior.JavaToDocSavior;
import cn.gudqs7.plugins.docer.theme.ThemeHelper;
import cn.gudqs7.plugins.util.PsiUtil;
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
            String qNameOfList = "java.util.List";
            String qNameOfMap = "java.util.Map";
            String qNameOfSet = "java.util.Set";
            boolean psiClassFromList = PsiUtil.isPsiClassFromXxx(psiClass, project, qNameOfList);
            boolean psiClassFromMap = PsiUtil.isPsiClassFromXxx(psiClass, project, qNameOfMap);
            boolean psiClassFromSet = PsiUtil.isPsiClassFromXxx(psiClass, project, qNameOfSet);
            // 不是 (list,map,set)
            return !(psiClassFromList
                    || psiClassFromMap
                    || psiClassFromSet);
        }
        return false;
    }

    @Override
    protected String getFileNamePrefix() {
        return "hsf-doc";
    }
}
