package cn.gudqs7.plugins.common.base.action.intention.helper;

import cn.gudqs7.plugins.common.util.jetbrain.PsiDocumentUtil;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * @author wq
 * @date 2022/6/3
 */
public interface CodeInsertHelper<T> {

    String SPACE = " ";
    String SPACE_TAB = "    ";
    String TAB = "\t";

    boolean supportElement(PsiElement psiElement);

    /**
     * 获取代码插入的位置
     *
     * @param psiElement 当前元素
     * @return 代码插入的位置
     */
    Integer getInsertOffset(@NotNull T psiElement);

    /**
     * 根据当前元素插入代码
     *
     * @param elementDocument    文档
     * @param psiDocumentManager 文档管理器
     * @param psiElement         当前元素
     * @param code               代码
     */
    default void insertCode(Document elementDocument, PsiDocumentManager psiDocumentManager,@NotNull T psiElement, String code) {
        Integer insertOffset = getInsertOffset(psiElement);
        if (insertOffset != null) {
            elementDocument.insertString(insertOffset, code);
            PsiDocumentUtil.commitAndSaveDocument(psiDocumentManager, elementDocument);
        }
    }

    /**
     * 根据当前元素获取前缀(即空格或 Tab), 以达成和格式化后的效果一致
     *
     * @param elementDocument 文档
     * @param psiElement      当前元素
     * @return 前缀(空格或 tab 组成)
     */
    String getPrefix(Document elementDocument,@NotNull T psiElement);

    /**
     * 根据当前元素获取前缀 - 前带换行
     *
     * @param elementDocument 文档
     * @param psiElement      当前元素
     * @return 前缀(空格或 tab 组成) 带换行
     */
    default String getPrefixWithBreakLine(Document elementDocument, @NotNull T psiElement) {
        return appendBreakLine(getPrefix(elementDocument, psiElement));
    }

    /**
     * 给前缀添加换行
     *
     * @param prefix 前缀
     * @return 添加换行后的前缀
     */
    default String appendBreakLine(String prefix) {
        if (prefix == null) {
            return null;
        }
        return "\n" + prefix;
    }

    /**
     * 获取行首到当前位置的内容
     *
     * @param document        文档
     * @param statementOffset 当前位置
     * @return 内容(一般为空格组成)
     */
    default String calculateSplitText(Document document, int statementOffset) {
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
        return splitText.toString();
    }

}
