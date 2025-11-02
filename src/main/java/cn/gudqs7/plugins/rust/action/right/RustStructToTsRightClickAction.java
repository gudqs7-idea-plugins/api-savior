package cn.gudqs7.plugins.rust.action.right;

import cn.gudqs7.plugins.rust.action.base.AbstractRustRightClickAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.rust.lang.core.psi.RsBlockFields;
import org.rust.lang.core.psi.RsFunction;
import org.rust.lang.core.psi.RsNamedFieldDecl;
import org.rust.lang.core.psi.RsStructItem;

import java.util.List;

public class RustStructToTsRightClickAction extends AbstractRustRightClickAction {
    /**
     * 根据方法判断是否应该展示
     *
     * @param rsFunction 方法
     * @param project    项目
     * @param e          e
     */
    @Override
    protected void checkRustFn(RsFunction rsFunction, Project project, AnActionEvent e) {
        notVisible(e);
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

    }

    /**
     * 根据类获取展示信息
     *
     * @param project      项目
     * @param rsStructItem 类
     * @return 展示信息
     */
    @Override
    protected String handleRustStruct0(Project project, RsStructItem rsStructItem) {
        StringBuilder tsSbf = new StringBuilder();
        RsBlockFields blockFields = rsStructItem.getBlockFields();
        List<RsNamedFieldDecl> namedFieldDeclList = blockFields.getNamedFieldDeclList();
        for (RsNamedFieldDecl namedFieldDecl : namedFieldDeclList) {
            String fieldName = namedFieldDecl.getName();
            if (namedFieldDecl.getTypeReference() != null) {
                String type = namedFieldDecl.getTypeReference().getText();
                System.out.println("type:" + type);
            }
            System.out.println("fieldName = " + fieldName);
        }

        return tsSbf.toString();
    }
}
