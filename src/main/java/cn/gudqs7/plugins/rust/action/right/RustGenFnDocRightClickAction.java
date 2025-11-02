package cn.gudqs7.plugins.rust.action.right;

import cn.gudqs7.plugins.rust.action.base.AbstractRustRightClickAction;
import cn.gudqs7.plugins.rust.helper.GenRustFnDocHelper;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.rust.lang.core.psi.RsFunction;
import org.rust.lang.core.psi.RsStructItem;

public class RustGenFnDocRightClickAction extends AbstractRustRightClickAction {
    /**
     * 根据方法判断是否应该展示
     *
     * @param rsFunction 方法
     * @param project    项目
     * @param e          e
     */
    @Override
    protected void checkRustFn(RsFunction rsFunction, Project project, AnActionEvent e) {
    }

    /**
     * 根据类信息判断是否应该展示
     *
     * @param rsStructItem 类
     * @param project     项目
     * @param e           e
     */
    @Override
    protected void checkRustStruct(RsStructItem rsStructItem, Project project, AnActionEvent e) {
        notVisible(e);
    }

    /**
     * 当在方法上右键时, 要做的操作
     *
     * @param project    项目
     * @param rsFunction 方法
     * @param editor     编辑器
     */
    @Override
    protected void handleRustFn(Project project, RsFunction rsFunction, Editor editor) {
        GenRustFnDocHelper.genRustFnDocBackground(rsFunction, editor);
    }
}
