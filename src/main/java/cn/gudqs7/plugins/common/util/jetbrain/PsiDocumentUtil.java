package cn.gudqs7.plugins.common.util.jetbrain;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.Variable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Psi 文档工具类
 * @author wenquan
 * @date 2021/9/30
 */
public class PsiDocumentUtil {

    public static void writeAndUpdateDocument(Document document, String insertText, AtomicInteger startOffset, @NotNull Editor editor) {
        if (org.apache.commons.lang.StringUtils.isNotBlank(insertText)) {
            // 在后台线程中执行写入操作
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                for (char c : insertText.toCharArray()) {
                    // 在 UI 线程中执行文档修改
                    ApplicationManager.getApplication().invokeLater(() -> {
                        WriteCommandAction.runWriteCommandAction(editor.getProject(), () -> {
                            int offset = startOffset.getAndIncrement();
                            document.insertString(offset, String.valueOf(c));

                            // 可选：滚动到插入位置
                            editor.getCaretModel().moveToOffset(offset + 1);
                            editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
                        });
                    });

                    // 延迟以创建流式效果
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }
    }

    public static void commitAndSaveDocument(PsiDocumentManager psiDocumentManager, Document document) {
        if (document != null) {
            psiDocumentManager.doPostponedOperationsAndUnblockDocument(document);
            psiDocumentManager.commitDocument(document);
            FileDocumentManager.getInstance().saveDocument(document);
        }
    }

    public static void addImportToFile(PsiDocumentManager psiDocumentManager, PsiJavaFile containingFile, Document document, Set<String> newImportList) {
        if (newImportList.size() > 0) {
            newImportList.removeIf(u -> u.startsWith("java.lang"));
        }

        if (newImportList.size() > 0) {
            PsiImportList importList = containingFile.getImportList();
            if (importList == null) {
                return;
            }
            PsiImportStatement[] importStatements = importList.getImportStatements();
            Set<String> containedSet = new HashSet<>();
            for (PsiImportStatement s : importStatements) {
                containedSet.add(s.getQualifiedName());
            }
            StringBuilder newImportText = new StringBuilder();
            for (String newImport : newImportList) {
                if (!containedSet.contains(newImport)) {
                    newImportText.append("\nimport ").append(newImport).append(";");
                }
            }
            PsiPackageStatement packageStatement = containingFile.getPackageStatement();
            int start = 0;
            if (packageStatement != null) {
                start = packageStatement.getTextLength() + packageStatement.getTextOffset();
            }
            String insertText = newImportText.toString();
            if (org.apache.commons.lang.StringUtils.isNotBlank(insertText)) {
                document.insertString(start, insertText);
                commitAndSaveDocument(psiDocumentManager, document);
            }
        }
    }

    public static void startTemplate(String insertCode, Editor editor, PsiFile containingFile, Variable... variableArray) {
        TemplateManager manager = TemplateManager.getInstance(containingFile.getProject());
        Template template = manager.createTemplate("", "", insertCode + "$END$");
        if (variableArray != null && variableArray.length > 0) {
            for (Variable variable : variableArray) {
                template.addVariable(variable);
            }
        }
        template.setToReformat(true);
        manager.startTemplate(editor, template);
    }

}
