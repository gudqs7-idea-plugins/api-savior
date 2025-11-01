package cn.gudqs7.plugins.rust.action.base;

import cn.gudqs7.plugins.common.base.action.AbstractAction;
import cn.gudqs7.plugins.common.util.WebEnvironmentUtil;
import cn.gudqs7.plugins.common.util.jetbrain.ClipboardUtil;
import cn.gudqs7.plugins.common.util.jetbrain.DialogUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.annotations.NotNull;
import org.rust.lang.core.psi.RsFunction;
import org.rust.lang.core.psi.RsStructItem;

/**
 * @author wq
 */
public abstract class AbstractRustRightClickAction extends AbstractAction {

    public void update0(@NotNull AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        if (project == null) {
            notVisible(e);
            return;
        }
        VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (virtualFile == null || editor == null) {
            notVisible(e);
            return;
        }
        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        if (psiFile == null) {
            notVisible(e);
            return;
        }
        PsiElement psiElement = psiFile.findElementAt(editor.getCaretModel().getOffset());
        if (psiElement == null) {
            notVisible(e);
            return;
        }
        RsFunction rustFn = getRustFn(psiElement);
        RsStructItem rustStruct = getRustStruct(psiElement);
        boolean isRightClickOnMethod = rustFn != null;
        boolean isRightClickOnStruct = rustStruct != null;

        // 啥也不是
        if (!isRightClickOnStruct && !isRightClickOnMethod) {
            notVisible(e);
            return;
        }

        if (isRightClickOnMethod) {
            checkRustFn(rustFn, project, e);
        }
        if (isRightClickOnStruct) {
            checkRustStruct(rustStruct, project, e);
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

            RsFunction rustFn = getRustFn(psiElement);
            RsStructItem rustStruct = getRustStruct(psiElement);
            boolean isRightClickOnMethod = rustFn != null;
            boolean isRightClickOnStruct = rustStruct != null;

            // 啥也不是
            if (!isRightClickOnStruct && !isRightClickOnMethod) {
                notVisible(e);
                return;
            }

            if (isRightClickOnMethod) {
                handleRustFn(project, rustFn);
            }
            if (isRightClickOnStruct) {
                handleRustStruct(project, rustStruct);
            }

        } finally {
            WebEnvironmentUtil.emptyIp();
        }
    }

    protected RsFunction getRustFn(PsiElement psiElement) {
        RsFunction rsFunction = null;
        if (psiElement instanceof PsiIdentifier) {
            psiElement = psiElement.getParent();
        }
        if (psiElement instanceof RsFunction) {
            rsFunction = (RsFunction) psiElement;
        }
        return rsFunction;
    }

    protected RsStructItem getRustStruct(PsiElement psiElement) {
        RsStructItem rsStructItem = null;
        if (psiElement instanceof LeafPsiElement) {
            psiElement = psiElement.getParent();
        }
        if (psiElement instanceof RsStructItem) {
            rsStructItem = (RsStructItem) psiElement;
        }
        return rsStructItem;
    }
    /**
     * 当在类上右键时, 要做的操作
     *
     * @param project  项目
     * @param psiClass 类
     */
    protected void handleRustStruct(Project project, RsStructItem psiClass) {
        String showContent = handleRustStruct0(project, psiClass);
        ClipboardUtil.setSysClipboardText(showContent);
        DialogUtil.showDialog(project, getTip(), showContent);
    }

    /**
     * 当在方法上右键时, 要做的操作
     *
     * @param project   项目
     * @param rsFunction 方法
     */
    protected void handleRustFn(Project project, RsFunction rsFunction) {
        String docByMethod = handleRustFn0(project, rsFunction);
        ClipboardUtil.setSysClipboardText(docByMethod);
        DialogUtil.showDialog(project, getTip(), docByMethod);
    }



    /**
     * 根据方法判断是否应该展示
     *
     * @param rsFunction 方法
     * @param project   项目
     * @param e         e
     */
    protected abstract void checkRustFn(RsFunction rsFunction, Project project, AnActionEvent e);

    /**
     * 根据类信息判断是否应该展示
     *
     * @param rsStructItem 类
     * @param project  项目
     * @param e        e
     */
    protected abstract void checkRustStruct(RsStructItem rsStructItem, Project project, AnActionEvent e);

    /**
     * 根据类获取展示信息
     *
     * @param project  项目
     * @param rsStructItem 类
     * @return 展示信息
     */
    protected String handleRustStruct0(Project project, RsStructItem rsStructItem) {
        return null;
    }

    /**
     * 根据方法获取展示信息
     *
     * @param project      项目
     * @param rsFunction    方法
     * @return 展示信息
     */
    protected String handleRustFn0(Project project, RsFunction rsFunction) {
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
