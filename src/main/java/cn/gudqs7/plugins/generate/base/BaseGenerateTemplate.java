package cn.gudqs7.plugins.generate.base;

import cn.gudqs7.plugins.common.base.postfix.template.AbstractPostfixTemplate;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * @author WQ
 */
public abstract class BaseGenerateTemplate extends AbstractPostfixTemplate {


    public BaseGenerateTemplate(String templateName, String example, Function<PsiElement, Boolean> isApplicable) {
        super(templateName, example, isApplicable);
    }

    public BaseGenerateTemplate(String templateName, String example, Function<PsiElement, Boolean> isApplicable, Function<PsiElement, PsiElement> expressionGetFn) {
        super(templateName, example, isApplicable, expressionGetFn);
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
    protected abstract BaseGenerate buildGenerate(PsiElement psiElement, PsiFile containingFile, PsiDocumentManager psiDocumentManager, Document document);

    @Override
    protected void expandForChooseExpression0(@NotNull PsiElement psiElement, @NotNull Editor editor) {
        Project project = psiElement.getProject();
        PsiFile containingFile = psiElement.getContainingFile();
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        Document document = psiDocumentManager.getDocument(containingFile);
        if (document == null) {
            return;
        }
        BaseGenerate baseGenerate = buildGenerate(psiElement, containingFile, psiDocumentManager, document);
        if (baseGenerate == null) {
            return;
        }
        removeExpressionFromEditor(psiElement, editor);
        baseGenerate.insertCodeWithTemplate(document, psiDocumentManager, containingFile, editor);
    }

}