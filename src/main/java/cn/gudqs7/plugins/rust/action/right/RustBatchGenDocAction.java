package cn.gudqs7.plugins.rust.action.right;

import cn.gudqs7.plugins.common.util.jetbrain.ExceptionUtil;
import cn.gudqs7.plugins.common.util.jetbrain.NotificationUtil;
import cn.gudqs7.plugins.rust.action.base.AbstractRustAction;
import cn.gudqs7.plugins.rust.helper.GenRustFnDocHelper;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.util.DocumentUtil;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.rust.lang.core.psi.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wq
 */
public class RustBatchGenDocAction extends AbstractRustAction {

    public void update0(@NotNull AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        if (project == null) {
            notVisible(e);
            return;
        }
        List<RsFile> rustFileList = getRustFileList(e);
        if (rustFileList.isEmpty()) {
            notVisible(e);
        }
    }

    @Override
    public void actionPerformed0(@NotNull AnActionEvent e) {
        try {
            Project project = e.getData(PlatformDataKeys.PROJECT);
            if (project == null) {
                return;
            }

            if (!ApplicationManager.getApplication().isDispatchThread()) {
                return;
            }

            List<RsFile> rustFileList = getRustFileList(e);
            if (rustFileList.isEmpty()) {
                return;
            }

            for (RsFile rsFile : rustFileList) {
                String fileName = rsFile.getName();
                new Task.Backgroundable(project, "给 " + fileName + " 生成函数文档中...", true) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        handleRustFile(rsFile, project, indicator);
                        NotificationUtil.showTips("文件 " + fileName + " 的函数文档生成完毕~");
                    }
                }.setCancelText("停止生成").queue();
            }

        } catch (Throwable throwable) {
            ExceptionUtil.handleException(throwable);
        }
    }

    protected List<RsFile> getRustFileList(AnActionEvent e) {
        List<RsFile> rsFileList = new ArrayList<>();
        // 处理单个的情况
        PsiElement psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);
        if (psiElement != null) {
            getRustFileBySingle(psiElement, rsFileList);
        }
        // 处理多个的情况
        PsiElement[] psiElements = e.getData(PlatformDataKeys.PSI_ELEMENT_ARRAY);
        if (psiElements != null && psiElements.length > 1) {
            for (PsiElement element : psiElements) {
                getRustFileBySingle(element, rsFileList);
            }
        }
        return rsFileList;
    }

    private void getRustFileBySingle(PsiElement psiElement, List<RsFile> rsFileList) {
        // 右键在 rust 文件上
        if (psiElement instanceof RsFile rsFile) {
            rsFileList.add(rsFile);
        }
        // 右键在目录上
        if (psiElement instanceof PsiDirectory psiDirectory) {
            walkPsiDirectory(psiDirectory, rsFileList);
        }
    }

    protected void walkPsiDirectory(PsiDirectory psiDirectory, List<RsFile> rsFileList) {
        if (psiDirectory != null) {
            @NotNull PsiElement[] children = psiDirectory.getChildren();
            for (PsiElement child : children) {
                if (child instanceof PsiDirectory psiDirectory0) {
                    walkPsiDirectory(psiDirectory0, rsFileList);
                }
                if (child instanceof RsFile rsFile) {
                    rsFileList.add(rsFile);
                }
            }
        }
    }

    protected void handleRustFile(RsFile rsFile, Project project, @NotNull ProgressIndicator indicator) {
        if (rsFile == null) {
            return;
        }
        List<RsFuncInfo> rsFuncInfoList = new ArrayList<>();
        ApplicationManager.getApplication().runReadAction(() -> {
            for (PsiElement psiElement : rsFile.getChildren()) {
                if (psiElement instanceof RsFunction rsFunction) {
                    addRsFun(rsFunction, rsFuncInfoList);
                }
                if (psiElement instanceof RsImplItem rsImplItem) {
                    RsMembers members = rsImplItem.getMembers();
                    if (members != null) {
                        addByMember(members, rsFuncInfoList);
                    }
                }
                if (psiElement instanceof RsTraitItem rsTraitItem) {
                    RsMembers members = rsTraitItem.getMembers();
                    if (members != null) {
                        addByMember(members, rsFuncInfoList);
                    }
                }
            }
        });

        int totalFunCount = rsFuncInfoList.size();
        for (int i = 0; i < rsFuncInfoList.size(); i++) {
            indicator.setFraction(i * 1d / totalFunCount);
            RsFuncInfo rsFuncInfo = rsFuncInfoList.get(i);
            handleRustFn(rsFile, rsFuncInfo, project, indicator);
        }

    }

    private static void addByMember(RsMembers members, List<RsFuncInfo> rsFuncInfoList) {
        @NotNull PsiElement[] children = members.getChildren();
        for (PsiElement child : children) {
            if (child instanceof RsFunction rsFunction) {
                addRsFun(rsFunction, rsFuncInfoList);
            }
        }
    }

    private static void addRsFun(RsFunction rsFunction, List<RsFuncInfo> rsFuncInfoList) {
        PsiElement firstChild = rsFunction.getFirstChild();
        boolean hasComment = firstChild instanceof PsiComment;
        if (hasComment) {
            return;
        }
        RsFuncInfo rsFuncInfo = new RsFuncInfo();
        rsFuncInfo.setText(rsFunction.getText());
        rsFuncInfo.setRsFunction(rsFunction);
        rsFuncInfoList.add(rsFuncInfo);
    }

    /**
     * 当在方法上右键时, 要做的操作
     *
     * @param rsFile 文件
     * @param rsFuncInfo 函数信息
     * @param project    项目
     * @param indicator  进度条
     */
    protected void handleRustFn(RsFile rsFile, RsFuncInfo rsFuncInfo, Project project, @NotNull ProgressIndicator indicator) {
        String text = rsFuncInfo.getText();
        RsFunction rsFunction = rsFuncInfo.getRsFunction();
        Document document = PsiDocumentManager.getInstance(project).getDocument(rsFile);
        if (document == null) {
            return;
        }
        AtomicInteger startOffset = new AtomicInteger(0);
        ApplicationManager.getApplication().runReadAction(() -> {
            int start = rsFunction.getTextRange().getStartOffset();
            int lineStartOffset = DocumentUtil.getLineStartOffset(start, document);
            startOffset.set(lineStartOffset - 1);
        });
        GenRustFnDocHelper.generateByAi(project, document, indicator, text, startOffset);
    }

    @Data
    public static class RsFuncInfo {

        private String text;
        private RsFunction rsFunction;

    }

}
