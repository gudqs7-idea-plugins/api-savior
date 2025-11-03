package cn.gudqs7.plugins.rust.action.base;

import cn.gudqs7.plugins.common.util.jetbrain.ClipboardUtil;
import cn.gudqs7.plugins.common.util.jetbrain.DialogUtil;
import cn.gudqs7.plugins.common.util.jetbrain.ExceptionUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.annotations.NotNull;
import org.rust.lang.core.psi.RsEnumItem;
import org.rust.lang.core.psi.RsFile;
import org.rust.lang.core.psi.RsFunction;
import org.rust.lang.core.psi.RsStructItem;

/**
 * @author wq
 */
public abstract class AbstractRustRightClickAction extends AbstractRustAction {

    protected PsiElement getPsiElementByCurEditor(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        if (project == null) {
            return null;
        }
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return null;
        }
        VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (virtualFile == null) {
            return null;
        }
        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        if (psiFile == null) {
            return null;
        }
        if (psiFile instanceof RsFile) {
            int offset = editor.getCaretModel().getOffset();
            return psiFile.findElementAt(offset);
        }
        return null;
    }

    public void update0(@NotNull AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        if (project == null) {
            notVisible(e);
            return;
        }
        PsiElement psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);
        if (psiElement == null) {
            psiElement = getPsiElementByCurEditor(e);
        }
        if (psiElement == null) {
            notVisible(e);
            return;
        }
        RsFunction rustFn = getRustFn(psiElement);
        RsStructItem rustStruct = getRustStruct(psiElement);
        RsEnumItem rsEnumItem = getRustEnum(psiElement);
        boolean isRightClickOnMethod = rustFn != null;
        boolean isRightClickOnStruct = rustStruct != null;
        boolean isRightClickOnEnum = rsEnumItem != null;

        // 啥也不是
        if (!isRightClickOnStruct && !isRightClickOnMethod && !isRightClickOnEnum) {
            notVisible(e);
            return;
        }

        if (isRightClickOnMethod) {
            checkRustFn(rustFn, project, e);
        }
        if (isRightClickOnStruct) {
            checkRustStruct(rustStruct, project, e);
        }
        if (isRightClickOnEnum) {
            checkRustEnum(rsEnumItem, project, e);
        }
    }

    @Override
    public void actionPerformed0(@NotNull AnActionEvent e) {
        try {
            Project project = e.getData(PlatformDataKeys.PROJECT);
            if (project == null) {
                return;
            }
            Editor editor = e.getData(CommonDataKeys.EDITOR);
            if (editor == null) {
                return;
            }
            PsiElement psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);
            if (psiElement == null) {
                psiElement = getPsiElementByCurEditor(e);
            }
            if (psiElement == null) {
                notVisible(e);
                return;
            }

            RsFunction rustFn = getRustFn(psiElement);
            RsStructItem rustStruct = getRustStruct(psiElement);
            RsEnumItem rsEnumItem = getRustEnum(psiElement);
            boolean isRightClickOnMethod = rustFn != null;
            boolean isRightClickOnStruct = rustStruct != null;
            boolean isRightClickOnEnum = rsEnumItem != null;

            // 啥也不是
            if (!isRightClickOnStruct && !isRightClickOnMethod && !isRightClickOnEnum) {
                return;
            }

            if (isRightClickOnMethod) {
                handleRustFn(project, rustFn, editor);
            }
            if (isRightClickOnStruct) {
                handleRustStruct(project, rustStruct, editor);
            }
            if (isRightClickOnEnum) {
                handleRustEnum(project, rsEnumItem, editor);
            }

        } catch (Throwable throwable) {
            ExceptionUtil.handleException(throwable);
        }
    }

    protected RsFunction getRustFn(PsiElement psiElement) {
        RsFunction rsFunction = null;
        if (psiElement instanceof LeafPsiElement) {
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

    protected RsEnumItem getRustEnum(PsiElement psiElement) {
        RsEnumItem rsEnumItem = null;
        if (psiElement instanceof RsEnumItem) {
            rsEnumItem = (RsEnumItem) psiElement;
        }
        return rsEnumItem;
    }

    /**
     * 当在类上右键时, 要做的操作
     *
     * @param project  项目
     * @param psiClass 类
     * @param editor 编辑器
     */
    protected void handleRustStruct(Project project, RsStructItem psiClass, Editor editor) {
        String showContent = handleRustStruct0(project, psiClass);
        ClipboardUtil.setSysClipboardText(showContent);
        DialogUtil.showDialog(project, getTip(), showContent);
    }

    /**
     * 当在方法上右键时, 要做的操作
     *
     * @param project    项目
     * @param rsFunction 方法
     * @param editor 编辑器
     */
    protected void handleRustFn(Project project, RsFunction rsFunction, Editor editor) {
        String docByMethod = handleRustFn0(project, rsFunction);
        ClipboardUtil.setSysClipboardText(docByMethod);
        DialogUtil.showDialog(project, getTip(), docByMethod);
    }

    protected void handleRustEnum(Project project, RsEnumItem rsEnumItem, Editor editor) {
        String showContent = handleRustEnum0(project, rsEnumItem);
        ClipboardUtil.setSysClipboardText(showContent);
        DialogUtil.showDialog(project, getTip(), showContent);
    }


    /**
     * 根据方法判断是否应该展示
     *
     * @param rsFunction 方法
     * @param project   项目
     * @param e         e
     */
    protected void checkRustFn(RsFunction rsFunction, Project project, AnActionEvent e) {
        notVisible(e);
    }

    /**
     * 根据类信息判断是否应该展示
     *
     * @param rsStructItem 类
     * @param project  项目
     * @param e        e
     */
    protected void checkRustStruct(RsStructItem rsStructItem, Project project, AnActionEvent e) {
        notVisible(e);
    }

    protected void checkRustEnum(RsEnumItem rsEnumItem, Project project, AnActionEvent e) {
        notVisible(e);
    }

    /**
     * 处理struct
     *
     * @param project  项目
     * @param rsStructItem 类
     * @return 展示信息
     */
    protected String handleRustStruct0(Project project, RsStructItem rsStructItem) {
        return null;
    }

    /**
     * 处理方法
     *
     * @param project      项目
     * @param rsFunction    方法
     * @return 展示信息
     */
    protected String handleRustFn0(Project project, RsFunction rsFunction) {
        return null;
    }

    /**
     * 处理枚举
     *
     * @param project      项目
     * @param rsEnumItem    枚举
     * @return 展示信息
     */
    protected String handleRustEnum0(Project project, RsEnumItem rsEnumItem) {
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
