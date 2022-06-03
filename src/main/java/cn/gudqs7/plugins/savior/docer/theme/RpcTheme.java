package cn.gudqs7.plugins.savior.docer.theme;

import cn.gudqs7.plugins.common.util.JsonUtil;
import cn.gudqs7.plugins.savior.docer.enums.ThemeType;
import cn.gudqs7.plugins.savior.docer.pojo.StructureAndCommentInfo;
import cn.gudqs7.plugins.savior.docer.pojo.annotation.CommentInfo;
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
        return ThemeType.RPC;
    }

    @Override
    public String getPathPrefix() {
        return "rpc";
    }

    @Override
    public void afterCollectData(Map<String, Object> dataByStr, Project project, PsiMethod publicMethod, String interfaceClassName, CommentInfo commentInfo, StructureAndCommentInfo paramStructureAndCommentInfo, StructureAndCommentInfo returnStructureAndCommentInfo, Map<String, Object> java2jsonMap, Map<String, Object> returnJava2jsonMap, String java2jsonStr, String returnJava2jsonStr) {
        if (java2jsonMap == null || java2jsonMap.size() == 0) {
            dataByStr.put("jsonExample", "");
            return;
        }
        List<Object> list = new ArrayList<>(java2jsonMap.values());
        java2jsonStr = JsonUtil.toJson(list);
        dataByStr.put("jsonExample", java2jsonStr);
    }

}
