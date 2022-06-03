package cn.gudqs7.plugins.savior.generate.base;

import cn.gudqs7.plugins.common.util.ActionUtil;
import cn.gudqs7.plugins.common.util.PsiUtil;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateExpressionSelectorBase;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateWithExpressionSelector;
import com.intellij.codeInsight.template.postfix.util.JavaPostfixTemplatesUtils;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

/**
 * @author WQ
 */
public abstract class GenerateBaseTemplate extends PostfixTemplateWithExpressionSelector {

    public GenerateBaseTemplate(String templateName, String example, Function<PsiElement, Boolean> isApplicable) {
        this(templateName, example, isApplicable, GenerateBaseTemplate::getExpressions);
    }

    public GenerateBaseTemplate(String templateName, String example, Function<PsiElement, Boolean> isApplicable, Function<PsiElement, PsiElement> expressionGetFn) {
        super(
                templateName,
                templateName,
                example,
                new PostfixTemplateExpressionSelectorBase(psiElement -> true) {
                    @Override
                    protected List<PsiElement> getNonFilteredExpressions(@NotNull PsiElement context, @NotNull Document document, int offset) {
                        return ContainerUtil.createMaybeSingletonList(expressionGetFn.apply(context));
                    }

                    @Override
                    protected Condition<PsiElement> getFilters(int offset) {
                        return psiElement -> true;
                    }

                    @Override
                    public boolean hasExpression(@NotNull PsiElement context, @NotNull Document copyDocument, int newOffset) {
                        return isApplicable.apply(context);
                    }
                },
                null
        );
    }

    /**
     * 获取传到 expandForChooseExpression 方法的 PsiElement 数据, 逻辑与 isApplicable 保持一致即可
     *
     * @param context 当前 psiElement
     * @return 传到 expandForChooseExpression 方法的 PsiElement
     */
    public static PsiElement getExpressions(PsiElement context) {
        return JavaPostfixTemplatesUtils.getTopmostExpression(context);
    }

    /**
     * 根据当前情况构建生成器
     *
     * @param psiElement         上下文
     * @param containingFile     Java 文件
     * @param psiDocumentManager 文档管理器
     * @param document           文档
     * @return 生成器
     */
    protected abstract GenerateBase buildGenerate(PsiElement psiElement, PsiFile containingFile, PsiDocumentManager psiDocumentManager, Document document);

    @Override
    protected void expandForChooseExpression(@NotNull PsiElement psiElement, @NotNull Editor editor) {
        try {
            Project project = psiElement.getProject();
            PsiFile containingFile = psiElement.getContainingFile();
            PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
            Document document = psiDocumentManager.getDocument(containingFile);
            if (document == null) {
                return;
            }
            GenerateBase generateBase = buildGenerate(psiElement, containingFile, psiDocumentManager, document);
            if (generateBase == null) {
                return;
            }
            removeExpressionFromEditor(psiElement, editor);
            generateBase.insertCodeByPsiTypeWithTemplate(document, psiDocumentManager, containingFile, editor);
            PsiUtil.clearGeneric();
        } catch (Exception e) {
            ActionUtil.handleException(e);
        }
    }

    protected void removeExpressionFromEditor(@NotNull PsiElement expression, @NotNull Editor editor) {
        while (!(expression.getParent() instanceof PsiCodeBlock)) {
            expression = expression.getParent();
        }
        Document document = editor.getDocument();
        TextRange textRange = expression.getTextRange();
        int endOffset = textRange.getEndOffset();
        String text = document.getText(new TextRange(endOffset, endOffset + 1));
        boolean expressionEndWithSemicolon = ";".equals(text);
        if (expressionEndWithSemicolon) {
            endOffset = endOffset + 1;
        }
        document.deleteString(textRange.getStartOffset(), endOffset);
    }
}