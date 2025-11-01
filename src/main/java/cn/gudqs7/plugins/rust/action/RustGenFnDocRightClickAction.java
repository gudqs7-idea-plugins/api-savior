package cn.gudqs7.plugins.rust.action;

import cn.gudqs7.plugins.rust.action.base.AbstractRustAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.rust.lang.core.psi.RsFunction;
import org.rust.lang.core.psi.RsPatStruct;

public class RustGenFnDocRightClickAction extends AbstractRustAction {
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
     * @param rsPatStruct 类
     * @param project     项目
     * @param e           e
     */
    @Override
    protected void checkRustStruct(RsPatStruct rsPatStruct, Project project, AnActionEvent e) {

    }

    /**
     * 根据方法获取展示信息
     *
     * @param project    项目
     * @param rsFunction 方法
     * @return 展示信息
     */
    @Override
    protected String handleRustFn0(Project project, RsFunction rsFunction) {
        return super.handleRustFn0(project, rsFunction);
    }
}
