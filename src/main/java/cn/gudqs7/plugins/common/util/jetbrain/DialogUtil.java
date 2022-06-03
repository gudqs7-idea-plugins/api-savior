package cn.gudqs7.plugins.common.util.jetbrain;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

/**
 * @author wq
 * @date 2022/6/3
 */
public class DialogUtil {

    public static boolean show = true;

    public static void showDialog(Project project, String tip, String content) {
        showDialog(project, tip, content, false);
    }

    public static void showDialog(Project project, String tip, String content, boolean force) {
        NotificationUtil.showTips("可以粘贴(Ctrl+V)了");
        if (force || show) {
            String title = "若希望之后不在弹出请点击确定";
            if (force) {
                title = "温馨提示";
            }
            String dialog = Messages.showMultilineInputDialog(project, tip, title, content, Messages.getInformationIcon(), null);
            if (dialog != null) {
                show = false;
            }
        }
    }

}
