package cn.gudqs7.plugins.savior.util;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Psi 文档工具类
 * @author wenquan
 * @date 2021/9/30
 */
public class PsiDocumentUtil {

    private static final String SPACE = " ";
    private static final String TAB = "\t";

    @NotNull
    public static String calculateSplitText(Document document, int statementOffset, String addition) {
        // 取得要计算的行有代码地方的初始 offset, 即 statementOffset
        // 根据这个offset 往前遍历取得其缩进, 可能为 空格或 \t
        // 若需要在此基础上再缩进一次, 可对参数 addition 赋值 4个空格
        StringBuilder splitText = new StringBuilder();
        int cur = statementOffset;
        String text = document.getText(new TextRange(cur - 1, cur));
        while (SPACE.equals(text) || TAB.equals(text)) {
            splitText.insert(0, text);
            cur--;
            if (cur < 1) {
                break;
            }
            text = document.getText(new TextRange(cur - 1, cur));
        }
        splitText.insert(0, "\n" + addition);
        return splitText.toString();
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

}
