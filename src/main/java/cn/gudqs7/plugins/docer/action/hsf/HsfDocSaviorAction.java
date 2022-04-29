package cn.gudqs7.plugins.docer.action.hsf;

import cn.gudqs7.plugins.docer.action.base.AbstractDocerSavior;
import cn.gudqs7.plugins.docer.annotation.AnnotationHolder;
import cn.gudqs7.plugins.docer.theme.ThemeHelper;
import cn.gudqs7.plugins.util.PsiClassUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;

/**
 * @author wq
 */
public class HsfDocSaviorAction extends AbstractDocerSavior {

    public HsfDocSaviorAction() {
        super(ThemeHelper.getHsfTheme());
    }

    @Override
    protected void checkPsiMethod(PsiMethod psiMethod, Project project, AnActionEvent e) {
        // 不带 mapping 注解的
        AnnotationHolder psiMethodHolder = AnnotationHolder.getPsiMethodHolder(psiMethod);
        boolean hasMappingAnnotation = psiMethodHolder.hasAnyOneAnnotation(AnnotationHolder.QNAME_OF_MAPPING, AnnotationHolder.QNAME_OF_GET_MAPPING, AnnotationHolder.QNAME_OF_POST_MAPPING, AnnotationHolder.QNAME_OF_PUT_MAPPING, AnnotationHolder.QNAME_OF_DELETE_MAPPING);
        if (hasMappingAnnotation) {
            notVisible(e);
        }
    }

    @Override
    protected void checkPsiClass(PsiClass psiClass, Project project, AnActionEvent e) {
        if (!PsiClassUtil.isNormalInterface(psiClass, project)) {
            notVisible(e);
        }
    }

}
