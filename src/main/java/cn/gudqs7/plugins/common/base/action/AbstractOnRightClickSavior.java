package cn.gudqs7.plugins.common.base.action;

import cn.gudqs7.plugins.common.resolver.comment.AnnotationHolder;
import cn.gudqs7.plugins.common.util.PluginSettingHelper;
import cn.gudqs7.plugins.common.util.WebEnvironmentUtil;
import cn.gudqs7.plugins.common.util.jetbrain.ClipboardUtil;
import cn.gudqs7.plugins.common.util.jetbrain.DialogUtil;
import cn.gudqs7.plugins.common.util.jetbrain.ExceptionUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

/**
 * @author wq
 */
public abstract class AbstractOnRightClickSavior extends AbstractAction implements UpdateInBackground {

    @Override
    public void update0(@NotNull AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        if (project == null) {
            notVisible(e);
            return;
        }
        PsiElement psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);
        if (psiElement == null) {
            notVisible(e);
            return;
        }

        PsiMethod psiMethod = getPsiMethod(psiElement);
        PsiClass psiClass = getPsiClass(psiElement);
        boolean isRightClickOnMethod = psiMethod != null;
        boolean isRightClickOnClass = psiClass != null;

        // 啥也不是
        if (!isRightClickOnClass && !isRightClickOnMethod) {
            notVisible(e);
            return;
        }

        if (isRightClickOnMethod) {
            checkPsiMethod(psiMethod, project, e);
        }
        if (isRightClickOnClass) {
            checkPsiClass(psiClass, project, e);
        }
    }

    @Override
    public void actionPerformed0(@NotNull AnActionEvent e) {
        try {
            Project project = e.getData(PlatformDataKeys.PROJECT);
            if (project == null) {
                return;
            }
            PsiElement psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);
            if (psiElement == null) {
                return;
            }

            PsiMethod psiMethod = getPsiMethod(psiElement);
            boolean isRightClickOnMethod = psiMethod != null;
            PsiClass psiClass = getPsiClass(psiElement);
            boolean isRightClickOnClass = psiClass != null;

            VirtualFile virtualFile = null;
            if (isRightClickOnClass) {
                virtualFile = psiClass.getContainingFile().getVirtualFile();
            }
            if (isRightClickOnMethod) {
                virtualFile = psiMethod.getContainingFile().getVirtualFile();
            }

            if (virtualFile != null) {
                PluginSettingHelper.initConfig(project, virtualFile);
            }

            if (isRightClickOnMethod) {
                handlePsiMethod(project, psiMethod);
                return;
            }

            if (isRightClickOnClass) {
                handlePsiClass(project, psiClass);
            }
        } finally {
            WebEnvironmentUtil.emptyIp();
        }
    }

    /**
     * 方法是否不带 Mapping 注解
     *
     * @param psiMethod 方法
     * @return 是否不带 Mapping 注解
     */
    protected boolean methodNotHaveMapping(PsiMethod psiMethod) {
        AnnotationHolder psiMethodHolder = AnnotationHolder.getPsiMethodHolder(psiMethod);
        return !psiMethodHolder.hasAnyOneAnnotation(AnnotationHolder.QNAME_OF_MAPPING, AnnotationHolder.QNAME_OF_GET_MAPPING, AnnotationHolder.QNAME_OF_POST_MAPPING, AnnotationHolder.QNAME_OF_PUT_MAPPING, AnnotationHolder.QNAME_OF_DELETE_MAPPING);
    }

    /**
     * 根据方法判断是否应该展示
     *
     * @param psiMethod 方法
     * @param project   项目
     * @param e         e
     */
    protected abstract void checkPsiMethod(PsiMethod psiMethod, Project project, AnActionEvent e);

    /**
     * 根据类信息判断是否应该展示
     *
     * @param psiClass 类
     * @param project  项目
     * @param e        e
     */
    protected abstract void checkPsiClass(PsiClass psiClass, Project project, AnActionEvent e);

    /**
     * 当在类上右键时, 要做的操作
     *
     * @param project  项目
     * @param psiClass 类
     */
    protected void handlePsiClass(Project project, PsiClass psiClass) {
        String showContent = handlePsiClass0(project, psiClass);
        ClipboardUtil.setSysClipboardText(showContent);
        DialogUtil.showDialog(project, getTip(), showContent);
    }

    /**
     * 当在方法上右键时, 要做的操作
     *
     * @param project   项目
     * @param psiMethod 方法
     */
    protected void handlePsiMethod(Project project, PsiMethod psiMethod) {
        PsiClass containingClass = psiMethod.getContainingClass();
        if (containingClass == null) {
            ExceptionUtil.handleSyntaxError(psiMethod.getName() + "'s Class");
        }
        String psiClassName = containingClass.getQualifiedName();
        String docByMethod = handlePsiMethod0(project, psiMethod, psiClassName);
        ClipboardUtil.setSysClipboardText(docByMethod);
        DialogUtil.showDialog(project, getTip(), docByMethod);
    }

    /**
     * 根据类获取展示信息
     *
     * @param project  项目
     * @param psiClass 类
     * @return 展示信息
     */
    protected String handlePsiClass0(Project project, PsiClass psiClass) {
        return null;
    }

    /**
     * 根据方法获取展示信息
     *
     * @param project      项目
     * @param psiMethod    方法
     * @param psiClassName 类名
     * @return 展示信息
     */
    protected String handlePsiMethod0(Project project, PsiMethod psiMethod, String psiClassName) {
        return null;
    }

    /**
     * 设置弹框中的首行提示
     *
     * @return 提示
     */
    protected String getTip() {
        return null;
    }

}
