package cn.gudqs7.plugins.rust.helper;

import cn.gudqs7.plugins.common.util.api.DeepSeekStreamHandler;
import cn.gudqs7.plugins.common.util.jetbrain.ExceptionUtil;
import cn.gudqs7.plugins.common.util.jetbrain.IdeaApplicationUtil;
import cn.gudqs7.plugins.common.util.jetbrain.PsiDocumentUtil;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.util.DocumentUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rust.lang.core.psi.RsFunction;

import java.util.concurrent.atomic.AtomicInteger;

public class GenRustFnDocHelper {

    public static void genRustFnDocBackground(RsFunction rsFunction, Editor editor) {
        Project project = editor.getProject();
        Document document = editor.getDocument();
        try {
            // 删除原注释
            PsiElement firstChild = rsFunction.getFirstChild();
            boolean hasComment = firstChild instanceof PsiComment;
            if (hasComment) {
                IdeaApplicationUtil.runWriteAction(project, () -> {
                    firstChild.delete();
                    PsiDocumentUtil.commitAndSaveDocumentEx(document, project);
                });
            }

            String text = rsFunction.getText();
            int start = rsFunction.getTextRange().getStartOffset();
            int lineStartOffset = DocumentUtil.getLineStartOffset(start, document);
            AtomicInteger startOffset = new AtomicInteger(lineStartOffset - 1);
            new Task.Backgroundable(project, "生成函数文档中...", true) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    generateByAi(project, document, indicator, text, startOffset);
                }
            }.setCancelText("停止生成").queue();
        } catch (Throwable e) {
            ExceptionUtil.handleException(e);
        }
    }

    public static void generateByAi(Project project, Document document, @Nullable ProgressIndicator indicator, String text, AtomicInteger startOffset) {
        IdeaApplicationUtil.runWriteAction(project, () -> {
            document.insertString(startOffset.getAndAdd(1), "\n");
        });
        DeepSeekStreamHandler.getInstance().streamChat(text, new DeepSeekStreamHandler.StreamCallback() {
            private final StringBuilder fullContent = new StringBuilder();

            @Override
            public void onContent(String content) {
                if (indicator != null) {
                    indicator.checkCanceled();
                    indicator.setText(content);
                    indicator.setText2(fullContent.toString());
                }

                fullContent.append(content);

                IdeaApplicationUtil.runWriteAction(project, () -> {
                    document.insertString(startOffset.getAndAdd(content.length()), content);
                });
            }

            @Override
            public void onFinish(String reason) {

            }

            @Override
            public void onComplete() {
//                System.out.println("\n\n=== 完整响应 ===\n" + fullContent);

                IdeaApplicationUtil.runWriteAction(project, () -> {
                    // 提交文档 刷新PSI
                    PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
                    psiDocumentManager.doPostponedOperationsAndUnblockDocument(document);
                    psiDocumentManager.commitDocument(document);
                });
            }

            @Override
            public void onError(Exception e) {
                if (e instanceof ProcessCanceledException) {
                    throw ((ProcessCanceledException) e);
                }
                ExceptionUtil.handleException(e);
            }
        });
    }


}
