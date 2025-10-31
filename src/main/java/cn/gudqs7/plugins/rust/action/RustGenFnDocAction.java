package cn.gudqs7.plugins.rust.action;

import cn.gudqs7.plugins.common.util.api.DeepSeekStreamHandler;
import cn.gudqs7.plugins.common.util.jetbrain.ExceptionUtil;
import cn.gudqs7.plugins.common.util.jetbrain.IdeaApplicationUtil;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import kotlin.jvm.internal.Intrinsics;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rust.ide.intentions.RsElementBaseIntentionAction;
import org.rust.lang.core.psi.RsFunction;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wq
 * @description Rust函数文档生成动作类，用于为Rust函数生成文档注释
 * @suppress IntentionDescriptionNotFoundInspection 警告抑制，表示未找到意图描述
 */
@SuppressWarnings("IntentionDescriptionNotFoundInspection")
public class RustGenFnDocAction extends RsElementBaseIntentionAction<RustGenFnDocAction.FunctionContext> {

    /**
     * 获取意图动作的家族名称
     * @return 返回"Generate function doc"作为家族名称
     */
    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "Generate function doc";
    }

    /**
     * 获取意图动作的文本描述
     * @return 返回"Generate function doc"作为文本描述
     */
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

                int start = rsFunction.getTextRange().getStartOffset(); // 获取函数文本的起始偏移量
                if (!ApplicationManager.getApplication().isWriteAccessAllowed()) {
                    document.insertString(start, "/// 无法预览\n"); // 如果没有权限，插入无预览提示
                    return;
                }

                AtomicInteger startOffset = new AtomicInteger(start);
                try {
                    DeepSeekStreamHandler.getInstance().streamChat(text, new DeepSeekStreamHandler.StreamCallback() {
                        private final StringBuilder fullContent = new StringBuilder();

                        @Override
                        public void onContent(String content) {
                            fullContent.append(content);
                            System.out.print(content);
                            System.out.flush();
                        }

                        @Override
                        public void onFinish(String reason) {
                            System.out.println("\n\n结束原因: " + reason);
                        }

                        @Override
                        public void onComplete() {
                            System.out.println("\n\n=== 完整响应 ===");
                            System.out.println(fullContent);

                            IdeaApplicationUtil.runWriteAction(() -> {
                                document.insertString(startOffset.getAndAdd(fullContent.length() + 1), fullContent + "\n");
                                editor.getCaretModel().moveToOffset(startOffset.get());
                                editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
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
