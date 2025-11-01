package cn.gudqs7.plugins.rust.action;

import cn.gudqs7.plugins.common.util.api.DeepSeekStreamHandler;
import cn.gudqs7.plugins.common.util.jetbrain.ExceptionUtil;
import cn.gudqs7.plugins.common.util.jetbrain.IdeaApplicationUtil;
import cn.gudqs7.plugins.common.util.jetbrain.PsiDocumentUtil;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import kotlin.jvm.internal.Intrinsics;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rust.ide.intentions.RsElementBaseIntentionAction;
import org.rust.lang.core.psi.RsFunction;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * rust 函数文档生成
 */
@SuppressWarnings("IntentionDescriptionNotFoundInspection")
public class RustGenFnDocIntentAction extends RsElementBaseIntentionAction<RustGenFnDocIntentAction.FunctionContext> {

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "Generate function doc";
    }

    @Override
    public @NotNull @IntentionName String getText() {
        return "Generate function doc";
    }

    /**
     * 查找适用的上下文
     * @param project 当前项目
     * @param editor 编辑器实例
     * @param psiElement PSI元素
     * @return 如果父元素是RsFunction则返回FunctionContext，否则返回null
     */
    @Nullable
    @Override
    public FunctionContext findApplicableContext(@NotNull Project project, @NotNull Editor editor, @NotNull PsiElement psiElement) {
        Intrinsics.checkNotNullParameter(project, "project");
        Intrinsics.checkNotNullParameter(editor, "editor");
        Intrinsics.checkNotNullParameter(psiElement, "element");

        if (psiElement.getParent() instanceof RsFunction rsFunction) {
            return new FunctionContext(rsFunction);
        }

        return null;
    }

    /**
     * 执行意图动作
     * @param project 当前项目
     * @param editor 编辑器实例
     * @param functionContext 函数上下文
     */
    @Override
    public void invoke(@NotNull Project project, @NotNull Editor editor, FunctionContext functionContext) {
        Intrinsics.checkNotNullParameter(project, "project");
        Intrinsics.checkNotNullParameter(editor, "editor");

        if (functionContext != null) {
            RsFunction rsFunction = functionContext.getRsFunction();
            if (rsFunction != null) {
                String text = rsFunction.getText(); // 获取函数的完整文本
                Document document = editor.getDocument();
                if (!ApplicationManager.getApplication().isWriteAccessAllowed()) {
                    document.insertString(0, "/// 无法预览\n"); // 如果没有权限，插入无预览提示
                    return;
                }
                PsiElement firstChild = rsFunction.getFirstChild();
                if (firstChild instanceof PsiComment) {
                    firstChild.delete();
                }
                PsiDocumentUtil.commitAndSaveDocumentEx(document, project);
                try {
                    new Task.Backgroundable(project, "Generating function doc", true) {

                        @Override
                        public void run(@NotNull ProgressIndicator indicator) {
                            // 获取函数文本的起始偏移量
                            int start = rsFunction.getTextRange().getStartOffset();
                            AtomicInteger startOffset = new AtomicInteger(start - 1);

                            IdeaApplicationUtil.runWriteAction(project, () -> {
                                document.insertString(startOffset.getAndAdd(1), "\n");
                            });

                            DeepSeekStreamHandler.getInstance().streamChat(text, new DeepSeekStreamHandler.StreamCallback() {
                                private final StringBuilder fullContent = new StringBuilder();

                                @Override
                                public void onContent(String content) {
                                    fullContent.append(content);
                                    System.out.print(content);
                                    System.out.flush();

                                    indicator.setText(content);
                                    indicator.setText2(fullContent.toString());
                                    IdeaApplicationUtil.runWriteAction(project, () -> {
                                        document.insertString(startOffset.getAndAdd(content.length()), content);
                                    });
                                }

                                @Override
                                public void onFinish(String reason) {
                                    System.out.println("\n\n结束原因: " + reason);
                                }

                                @Override
                                public void onComplete() {
                                    System.out.println("\n\n=== 完整响应 ===");
                                    System.out.println(fullContent);

                                    IdeaApplicationUtil.runWriteAction(project, () -> {
                                        PsiDocumentUtil.commitAndSaveDocumentEx(document, project);
                                    });
                                }

                                @Override
                                public void onError(Exception e, String error) {
                                    System.err.println("错误: " + error);
                                    if (e instanceof ProcessCanceledException) {
                                        throw ((ProcessCanceledException) e);
                                    }
                                }
                            });

                        }
                    }
                            .setCancelText("停止生成")
                            .queue();
                } catch (Throwable e) {
                    ExceptionUtil.handleException(e);
                }
            }
        }
    }

    @Data
    public static class FunctionContext {

        private RsFunction rsFunction;

        public FunctionContext() {
        }

        public FunctionContext(RsFunction rsFunction) {
            this.rsFunction = rsFunction;
        }
    }

}
