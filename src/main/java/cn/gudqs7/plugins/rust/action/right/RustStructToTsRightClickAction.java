package cn.gudqs7.plugins.rust.action.right;

import cn.gudqs7.plugins.common.util.StringTool;
import cn.gudqs7.plugins.rust.action.base.AbstractRustRightClickAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.rust.lang.core.psi.*;

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
     * 根据 struct 生成 ts
     *
     * @param project      项目
     * @param rsStructItem 类
     * @return 展示信息
     */
    @Override
    protected String handleRustStruct0(Project project, RsStructItem rsStructItem) {
        StringBuilder tsSbf = new StringBuilder();
        String structName = rsStructItem.getName();
        tsSbf.append("\nexport interface ").append(structName);
        RsTypeParameterList typeParameterList = rsStructItem.getTypeParameterList();
        if (typeParameterList != null) {
            tsSbf.append(rsStructItem.getTypeParameterList().getText());
        }
        tsSbf.append(" {\n");
        RsBlockFields blockFields = rsStructItem.getBlockFields();
        List<RsNamedFieldDecl> namedFieldDeclList = blockFields.getNamedFieldDeclList();
        for (RsNamedFieldDecl namedFieldDecl : namedFieldDeclList) {
            RsTypeReference typeReference = namedFieldDecl.getTypeReference();
            if (typeReference == null) {
                continue;
            }
            String fieldName = namedFieldDecl.getName();
            // todo typeReference 是 struct 时遍历
            String type = typeReference.getText();

            String tsType = type;
            boolean isOptional = false;
            switch (type) {
                case "OptLong", "OptInt",
                     "OptDouble", "OptFloat",
                     "Option<i64>", "Option<i32>",
                     "Option<f64>", "Option<f32>" -> {
                    isOptional = true;
                    tsType = "number";
                }
                case "OptBool", "Option<bool>" -> {
                    isOptional = true;
                    tsType = "boolean";
                }
                case "OptStr", "Option<String>" -> {
                    isOptional = true;
                    tsType = "string";
                }
                case "i64", "i32", "f64", "f32" -> {
                    tsType = "number";
                }
                case "bool" -> {
                    tsType = "boolean";
                }
                case "String", "&str" -> {
                    tsType = "string";
                }
                default -> {
                    if (type.contains("Option")) {
                        isOptional = true;
                        tsType = type.substring(7, type.length() - 1);
                    }
                }
            }

            String optStr = "";
            if (isOptional) {
                optStr = "?";
            }
            tsSbf.append("    ").append(StringTool.lineToCamelCase(fieldName)).append(optStr)
                    .append(": ").append(tsType).append(";\n");

//            System.out.println("type:" + type);
        }
        tsSbf.append("}\n");
        return tsSbf.toString();
    }

    /**
     * 设置弹框中的首行提示
     *
     * @return 提示
     */
    @Override
    protected String getTip() {
        return "TS 已生成且已复制到剪切板! 预览如下";
    }
}
