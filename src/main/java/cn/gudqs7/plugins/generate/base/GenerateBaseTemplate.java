package cn.gudqs7.plugins.generate.base;

import cn.gudqs7.plugins.docer.util.ActionUtil;
import cn.gudqs7.plugins.util.PsiUtil;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateExpressionSelectorBase;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateWithExpressionSelector;
import com.intellij.codeInsight.template.postfix.util.JavaPostfixTemplatesUtils;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.TextRange;
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
        super(
                templateName,
                templateName,
                example,
                new PostfixTemplateExpressionSelectorBase(psiElement -> true) {
                    @Override
                    protected List<PsiElement> getNonFilteredExpressions(@NotNull PsiElement context, @NotNull Document document, int offset) {
                        return ContainerUtil.createMaybeSingletonList(JavaPostfixTemplatesUtils.getTopmostExpression(context));
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