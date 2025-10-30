package cn.gudqs7.plugins.rust.action;

import cn.gudqs7.plugins.common.util.api.DeepSeekStreamHandler;
import cn.gudqs7.plugins.common.util.jetbrain.PsiDocumentUtil;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.util.DocumentUtil;
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
 */
@SuppressWarnings("IntentionDescriptionNotFoundInspection")
public class RustGenFnDocAction extends RsElementBaseIntentionAction<RustGenFnDocAction.FunctionContext> {

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "Generate function doc";
    }

    @Override
    public @NotNull @IntentionName String getText() {
        return "Generate function doc";
    }

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

    @Override
    public void invoke(@NotNull Project project, @NotNull Editor editor, FunctionContext functionContext) {
        Intrinsics.checkNotNullParameter(project, "project");
        Intrinsics.checkNotNullParameter(editor, "editor");

        if (functionContext != null) {
            RsFunction rsFunction = functionContext.getRsFunction();
            if (rsFunction != null) {
                String text = rsFunction.getText();

                Document document = editor.getDocument();
                DeepSeekStreamHandler handler = new DeepSeekStreamHandler("sk-94918533df50453780d40f09e99e4506");

                int start = rsFunction.getTextRange().getStartOffset();

                if (!ApplicationManager.getApplication().isWriteAccessAllowed()) {
                    document.insertString(start, "/// 无预览\n");
                    return;
                }

                AtomicInteger startOffset = new AtomicInteger(start);
                ApplicationManager.getApplication().runWriteAction(() -> {
                    try {
                        handler.streamChat(text, new DeepSeekStreamHandler.StreamCallback() {
                            private final StringBuilder fullContent = new StringBuilder();

                            @Override
                            public void onContent(String content) {
                                fullContent.append(content);
                                System.out.print(content);
                                System.out.flush();

                                // 在UI线程中写入
//                                PsiDocumentUtil.writeAndUpdateDocument(document, content, startOffset, editor);

                                // 滚动到插入的位置
                                document.insertString(startOffset.getAndAdd(content.length()), content);
                                editor.getCaretModel().moveToOffset(startOffset.get());
                                editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);

                            }

                            @Override
                            public void onFinish(String reason) {
                                System.out.println("\n\n结束原因: " + reason);
                            }

                            @Override
                            public void onComplete() {
                                System.out.println("\n\n=== 完整响应 ===");
                                System.out.println(fullContent);

                                // 滚动到插入的位置
                                PsiDocumentUtil.writeAndUpdateDocument(document, "\n", startOffset, editor);
                                editor.getCaretModel().moveToOffset(startOffset.get());
                                editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
                            }

                            @Override
                            public void onError(String error) {
                                System.err.println("错误: " + error);
                            }
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                System.out.println("fn text:\n" + text);
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
