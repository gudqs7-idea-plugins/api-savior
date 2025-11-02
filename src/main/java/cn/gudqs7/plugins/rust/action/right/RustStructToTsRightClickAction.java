package cn.gudqs7.plugins.rust.action.right;

import cn.gudqs7.plugins.common.util.StringTool;
import cn.gudqs7.plugins.rust.action.base.AbstractRustRightClickAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rust.lang.core.psi.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RustStructToTsRightClickAction extends AbstractRustRightClickAction {

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

    @Override
    protected void checkRustEnum(RsEnumItem rsEnumItem, Project project, AnActionEvent e) {

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
            tsSbf.append(typeParameterList.getText());
        }
        tsSbf.append(" {\n");

        SerdeInfo serdeInfo = getSerdeInfo(rsStructItem.getOuterAttrList());

        RsBlockFields blockFields = rsStructItem.getBlockFields();
        if (blockFields != null) {
            List<RsNamedFieldDecl> namedFieldDeclList = blockFields.getNamedFieldDeclList();
            genFieldByNameFieldList(namedFieldDeclList, tsSbf, "    ", serdeInfo.renameAll());
        }
        tsSbf.append("}\n");
        return tsSbf.toString();
    }

    private void genFieldByNameFieldList(List<RsNamedFieldDecl> namedFieldDeclList, StringBuilder tsSbf, String space, String renameType) {
        for (RsNamedFieldDecl namedFieldDecl : namedFieldDeclList) {
            String fieldName = namedFieldDecl.getName();
            TsTypeResult result = getTsTypeResult(namedFieldDecl.getTypeReference());
            if (result == null) {
                continue;
            }
            tsSbf.append(space).append(rename(fieldName, renameType)).append(result.optStr())
                    .append(": ").append(result.tsType()).append(";\n");
        }
    }

    private @Nullable TsTypeResult getTsTypeResult(RsTypeReference typeReference) {
        if (typeReference == null) {
            return null;
        }
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
        return new TsTypeResult(tsType, optStr);
    }

    private record TsTypeResult(String tsType, String optStr) {
    }

    @Override
    protected String handleRustEnum0(Project project, RsEnumItem rsEnumItem) {
        StringBuilder tsSbf = new StringBuilder();
        String enumName = rsEnumItem.getName();
        SerdeInfo serdeInfo = getSerdeInfo(rsEnumItem.getOuterAttrList());

        if (serdeInfo.serdeTag().isEmpty()) {
            return "未指定 tag 的枚举, 无法生成";
        }

        tsSbf.append("\nexport type ").append(enumName);
        RsTypeParameterList typeParameterList = rsEnumItem.getTypeParameterList();
        if (typeParameterList != null) {
            tsSbf.append(typeParameterList.getText());
        }
        tsSbf.append(" =\n");

        RsEnumBody enumBody = rsEnumItem.getEnumBody();
        if (enumBody != null) {
            List<RsEnumVariant> enumVariantList = enumBody.getEnumVariantList();
            for (RsEnumVariant rsEnumVariant : enumVariantList) {
                String enumItemName = rsEnumVariant.getIdentifier().getText();

                tsSbf.append("    | {\n");
                tsSbf.append(serdeInfo.serdeTag()).append(": '").append(rename(enumItemName, serdeInfo.renameAll())).append("';\n");

                boolean notInnerMode = !serdeInfo.serdeContent().isEmpty();

                RsBlockFields blockFields = rsEnumVariant.getBlockFields();
                if (blockFields != null) {
                    String space = "    ";
                    if (notInnerMode) {
                        space = "        ";
                        tsSbf.append("    ").append(serdeInfo.serdeContent()).append(": {\n");
                    }
                    List<RsNamedFieldDecl> namedFieldDeclList = blockFields.getNamedFieldDeclList();
                    genFieldByNameFieldList(namedFieldDeclList, tsSbf, space, serdeInfo.renameAllFields());
                    if (notInnerMode) {
                        tsSbf.append("    };\n");
                    }
                }

                RsTupleFields tupleFields = rsEnumVariant.getTupleFields();
                if (tupleFields != null) {
                    List<RsTupleFieldDecl> tupleFieldDeclList = tupleFields.getTupleFieldDeclList();
                    int tupleSize = tupleFieldDeclList.size();
                    boolean singleTuple = tupleSize == 1;
                    if (singleTuple) {
                        RsTupleFieldDecl rsTupleFieldDecl = tupleFieldDeclList.get(0);
                        TsTypeResult tsTypeResult = getTsTypeResult(rsTupleFieldDecl.getTypeReference());
                        if (tsTypeResult == null) {
                            tsTypeResult = new TsTypeResult("null", "");
                        }
                        if (notInnerMode) {
                            tsSbf.append("    ").append(serdeInfo.serdeContent()).append(tsTypeResult.optStr())
                                    .append(": ").append(tsTypeResult.tsType()).append(";\n");
                        } else {
                            tsSbf.append("    ").append("0").append(tsTypeResult.optStr())
                                    .append(": ").append(tsTypeResult.tsType()).append(";\n");
                        }
                    } else {
                        if (notInnerMode) {
                            tsSbf.append("    ").append(serdeInfo.serdeContent()).append(": (");
                        }
                        Set<String> typeSet = new HashSet<>();
                        for (int i = 0; i < tupleSize; i++) {
                            RsTupleFieldDecl rsTupleFieldDecl = tupleFieldDeclList.get(i);
                            TsTypeResult tsTypeResult = getTsTypeResult(rsTupleFieldDecl.getTypeReference());
                            if (tsTypeResult == null) {
                                continue;
                            }
                            String tsType = tsTypeResult.tsType();
                            if (notInnerMode) {
                                if (typeSet.contains(tsType)) {
                                    continue;
                                }
                                tsSbf.append(tsType);
                                typeSet.add(tsType);
                                if (i != tupleSize - 1) {
                                    tsSbf.append(" | ");
                                }
                            } else {
                                tsSbf.append("    ").append(i).append(tsTypeResult.optStr())
                                        .append(": ").append(tsType).append(";\n");
                            }
                        }
                        if (notInnerMode) {
                            tsSbf.append(")[];\n");
                        }
                    }
                }


                tsSbf.append("}\n");
            }
        }

        return tsSbf.toString();
    }

    private static @NotNull SerdeInfo getSerdeInfo(List<RsOuterAttr> outerAttrList) {
        String serdeTag = "";
        String serdeContent = "";
        String renameAll = "";
        String renameAllFields = "";

        // 获取 serde, 判断要以何种方式序列化, 不指定则无法生成
        //  1.内部, 仅指定 tag
        //  2.相邻, 指定 tag 和 content; 只会有两个字段
        //  3.内部模式不支持 元组字段
        for (RsOuterAttr rsOuterAttr : outerAttrList) {
            RsMetaItem metaItem = rsOuterAttr.getMetaItem();
            RsPath path = metaItem.getPath();
            if (path == null) {
                continue;
            }
            if (!"serde".equals(path.getText())) {
                continue;
            }
            RsMetaItemArgs metaItemArgs = metaItem.getMetaItemArgs();
            if (metaItemArgs == null) {
                continue;
            }

            List<RsMetaItem> metaItemList = metaItemArgs.getMetaItemList();
            for (RsMetaItem rsMetaItem : metaItemList) {
                RsLitExpr litExpr = rsMetaItem.getLitExpr();
                RsPath rsMetaItemPath = rsMetaItem.getPath();
                if (litExpr == null || rsMetaItemPath == null) {
                    continue;
                }
                String attrId = rsMetaItemPath.getText();
                String attrVal = litExpr.getText();
                if (attrVal.isEmpty()) {
                    continue;
                }
                if (attrVal.contains("\"")) {
                    attrVal = attrVal.replace("\"", "");
                }

                if ("rename_all".equals(attrId)) {
                    renameAll = attrVal;
                }
                if ("rename_all_fields".equals(attrId)) {
                    renameAllFields = attrVal;
                }
                if ("tag".equals(attrId)) {
                    serdeTag = attrVal;
                }
                if ("content".equals(attrId)) {
                    serdeContent = attrVal;
                }
            }
        }
        return new SerdeInfo(serdeTag, serdeContent, renameAll, renameAllFields);
    }

    private record SerdeInfo(String serdeTag, String serdeContent, String renameAll, String renameAllFields) {
    }

    protected String rename(String origin, String renameType) {
        if ("camelCase".equals(renameType)) {
            origin = StringTool.lineToCamelCase(origin);
        }
        // todo 实现其他重命名规则
        return origin;
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
