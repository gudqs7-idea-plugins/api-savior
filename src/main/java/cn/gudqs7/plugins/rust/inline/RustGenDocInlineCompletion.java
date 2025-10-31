package cn.gudqs7.plugins.rust.inline;

import com.intellij.codeInsight.inline.completion.InlineCompletionElement;
import com.intellij.codeInsight.inline.completion.InlineCompletionEvent;
import com.intellij.codeInsight.inline.completion.InlineCompletionProvider;
import com.intellij.codeInsight.inline.completion.InlineCompletionRequest;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilBase;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.DelayKt;
import kotlinx.coroutines.flow.Flow;
import kotlinx.coroutines.flow.FlowCollector;
import kotlinx.coroutines.flow.FlowKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rust.lang.core.psi.RsFunction;
import kotlinx.coroutines.Delay;

public class RustGenDocInlineCompletion implements InlineCompletionProvider {

    public RustGenDocInlineCompletion() {
        super();
    }

    @Nullable
    @Override
    public Object getProposals(@NotNull InlineCompletionRequest inlineCompletionRequest, @NotNull Continuation<? super Flow<InlineCompletionElement>> continuation) {
        Editor editor = inlineCompletionRequest.getEditor();
        int offset = inlineCompletionRequest.getStartOffset();

        Project project = editor.getProject();
        if (project == null) {
            return FlowKt.emptyFlow();
        }

        PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
        if (psiFile == null) {
            return FlowKt.emptyFlow();
        }

        PsiElement element = psiFile.findElementAt(offset);
        if (element == null) {
            return FlowKt.emptyFlow();
        }

        // 查找当前元素是否在函数定义内
        RsFunction rsFunction = findParentFunction(element);
        if (rsFunction == null) {
            return FlowKt.emptyFlow();
        }

        // 生成示例文档（实际应用中应该调用 AI API）
        String documentation = generateDocumentationSuggestion(rsFunction);

        Flow<InlineCompletionElement> flow = FlowKt.flow(new Function2<FlowCollector<? super InlineCompletionElement>, Continuation<? super Unit>, Object>() {
            @Override
            public Object invoke(FlowCollector<? super InlineCompletionElement> flowCollector, Continuation<? super Unit> continuation) {
                for (int i = 0; i < 10; i++) {
                    System.out.println("channelFlow for in");

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    InlineCompletionElement element1 = new InlineCompletionElement("test-" + i + "\n");
                    Flow<InlineCompletionElement> flow = FlowKt.flowOf(element1);
                    continuation.resumeWith(flow);
                    System.out.println("channelFlow for end");
                }
                return null;
            }
        });
        InlineCompletionElement element1 = new InlineCompletionElement(documentation);
        Flow<InlineCompletionElement> flow1 = FlowKt.flowOf(element1);
        continuation.resumeWith(flow1);
        return null;
    }

    @Override
    public boolean isEnabled(@NotNull InlineCompletionEvent inlineCompletionEvent) {
        // 启用内联补全功能
        return true;
    }

    /**
     * 查找父函数
     */
    private RsFunction findParentFunction(PsiElement element) {
        PsiElement current = element;
        while (current != null) {
            if (current instanceof RsFunction) {
                return (RsFunction) current;
            }
            current = current.getParent();
        }
        return null;
    }

    /**
     * 生成文档建议
     */
    private String generateDocumentationSuggestion(RsFunction rsFunction) {
        // 这里应该实际调用 AI API 来生成文档
        // 现在返回示例文档

        StringBuilder docBuilder = new StringBuilder();
        docBuilder.append("/// Description of the function\n");
        docBuilder.append("/// \n");
        docBuilder.append("/// # Arguments\n");
        docBuilder.append("/// \n");
        docBuilder.append("/// # Returns\n");
        docBuilder.append("/// \n");
        docBuilder.append("/// # Example\n");
        docBuilder.append("/// \n");
        docBuilder.append("/// ```\n");
        docBuilder.append("/// // Example usage\n");
        docBuilder.append("/// ```\n");

        return docBuilder.toString();
    }
}