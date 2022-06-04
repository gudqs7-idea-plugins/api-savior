package cn.gudqs7.plugins.common.util.jetbrain;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.WindowManager;

import java.awt.*;

/**
 * 项目相关工具类
 *
 * @author wq
 * @date 2022/6/4
 */
public class ProjectUtil {

    public static Project getActiveProject() {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        Project activeProject = null;
        for (Project project : projects) {
            Window window = WindowManager.getInstance().suggestParentWindow(project);
            if (window != null && window.isActive()) {
                activeProject = project;
            }
        }
        return activeProject;
    }

}
