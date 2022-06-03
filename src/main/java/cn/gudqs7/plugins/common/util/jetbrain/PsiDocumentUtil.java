package cn.gudqs7.plugins.common.util.jetbrain;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.psi.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Psi 文档工具类
 * @author wenquan
 * @date 2021/9/30
 */
public class PsiDocumentUtil {

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
