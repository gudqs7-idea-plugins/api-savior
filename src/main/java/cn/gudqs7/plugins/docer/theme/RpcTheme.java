package cn.gudqs7.plugins.docer.theme;

import cn.gudqs7.plugins.docer.constant.ThemeType;
import cn.gudqs7.plugins.docer.pojo.StructureAndCommentInfo;
import cn.gudqs7.plugins.docer.pojo.annotation.CommentInfo;
import cn.gudqs7.plugins.docer.util.JsonUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author wq
 */
public class RpcTheme implements Theme {

    private RpcTheme() {
    }

    private static RpcTheme instance;

    public static RpcTheme getInstance() {
        if (instance == null) {
            synchronized (RpcTheme.class) {
                if (instance == null) {
                    instance = new RpcTheme();
                }
            }
        }
        return instance;
    }

    @Override
    public ThemeType getThemeType() {
        return ThemeType.HSF;
    }

    @Override
    public String getPathPrefix() {
        return "rpc";
    }

    //
//    @Override
//    public String printByGoMap(Map<Integer, List<FieldLevelInfo>> goMap, boolean returnParam) {
//        StringBuilder all = new StringBuilder();
//        for (Map.Entry<Integer, List<FieldLevelInfo>> entry : goMap.entrySet()) {
//            Integer key = entry.getKey();
//
//            String[] numberToCnArray = new String[]{"", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
//            String cn = key + "";
//            if (numberToCnArray.length - 1 >= key) {
//                cn = numberToCnArray[key];
//            }
//            // level = 0 的数据过滤
//            if ("".equals(cn)) {
//                continue;
//            }
//            String levelTitle = "### 第" + cn + "层\n\n";
//            all.append(levelTitle);
//            List<FieldLevelInfo> fieldLevelInfoList = entry.getValue();
//            for (FieldLevelInfo fieldLevelInfo : fieldLevelInfoList) {
//                List<FieldMemoInfo> fieldMemoInfos = fieldLevelInfo.getAllFields();
//                String cn1 = fieldLevelInfo.getCn();
//                String en = fieldLevelInfo.getEn();
//                Map<String, String> data = new HashMap<>(16);
//                data.put("fieldName", "字段");
//                data.put("fieldType", "类型");
//                data.put("required", "必填性");
//                data.put("fieldDesc", "含义");
//                data.put("notes", "其他信息参考");
//                data.put("clazzTypeName", en);
//                data.put("addition", cn1);
//
//                String allFields = fieldMemoInfos.stream().map(FieldMemoInfo::getLine).collect(Collectors.joining());
//                data.put("allFields", allFields);
//                String result = BaseSavior.getTemplate(getParamTitlePath(returnParam), data);
//                all.append(result);
//            }
//        }
//        String allStr = all.toString();
//        if (StringUtils.isBlank(allStr)) {
//            return handleNoField(returnParam);
//        }
//        return allStr;
//    }

    @Override
    public void afterCollectData(Map<String, Object> dataByStr, Project project, PsiMethod publicMethod, String interfaceClassName, CommentInfo commentInfo, StructureAndCommentInfo paramStructureAndCommentInfo, StructureAndCommentInfo returnStructureAndCommentInfo, Map<String, Object> java2jsonMap, Map<String, Object> returnJava2jsonMap, String java2jsonStr, String returnJava2jsonStr) {
        if (java2jsonMap==null || java2jsonMap.size() == 0) {
            dataByStr.put("jsonExample", "");
            return;
        }
        List<Object> list = new ArrayList<>(java2jsonMap.values());
        java2jsonStr = JsonUtil.toJson(list);
        dataByStr.put("jsonExample", java2jsonStr);
    }

}
