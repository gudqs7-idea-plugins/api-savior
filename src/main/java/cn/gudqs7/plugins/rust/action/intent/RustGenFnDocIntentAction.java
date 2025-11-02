package cn.gudqs7.plugins.rust.action.intent;

import cn.gudqs7.plugins.rust.helper.GenRustFnDocHelper;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import kotlin.jvm.internal.Intrinsics;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rust.ide.intentions.RsElementBaseIntentionAction;
import org.rust.lang.core.psi.RsFunction;

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
                Document document = editor.getDocument();
                if (!ApplicationManager.getApplication().isWriteAccessAllowed()) {
                    document.insertString(0, "/// AI生成, 无法预览\n");
                    return;
                }
                GenRustFnDocHelper.genRustFnDocBackground(rsFunction, editor);
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
