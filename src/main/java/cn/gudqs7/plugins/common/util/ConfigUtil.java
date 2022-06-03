package cn.gudqs7.plugins.common.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author wq
 * @date 2021/9/21
 */
public class ConfigUtil {

    public static Map<String, String> getConfig(String configName, Project project, VirtualFile currentVirtualFile) {
        try {
            String defaultConfigPath = project.getBasePath() + File.separator + configName;
            VirtualFile virtualFileByDefault = LocalFileSystem.getInstance().findFileByPath(defaultConfigPath);
            if (virtualFileByDefault != null) {
                Properties properties = new Properties();
                properties.load(virtualFileByDefault.getInputStream());
                return toMap(properties);
            }
            PsiFile[] filesByName = FilenameIndex.getFilesByName(project, configName, GlobalSearchScope.projectScope(project));
            if (filesByName.length > 0) {
                Properties back = null;
                for (PsiFile psiFile : filesByName) {
                    VirtualFile virtualFile = psiFile.getVirtualFile();
                    Properties properties = new Properties();
                    properties.load(virtualFile.getInputStream());

                    String path = currentVirtualFile.getPath();
                    String configFilePath = virtualFile.getPath();
                    String projectBasePath1 = getProjectBasePath(path);
                    String projectBasePath2 = getProjectBasePath(configFilePath);
                    if (projectBasePath1.equals(projectBasePath2)) {
                        return toMap(properties);
                    }
                    if (back == null) {
                        back = properties;
                    }
                }
                return toMap(back);
            }
        } catch (Exception e) {
            ActionUtil.handleException(e);
        }
        return null;
    }

    private static Map<String, String> toMap(Properties properties) {
        Map<String, String> map = new HashMap<>(8);
        for (Object key : properties.keySet()) {
            Object val = properties.get(key);
            map.put(String.valueOf(key), String.valueOf(val));
        }
        return map;
    }

    private static String getProjectBasePath(String path) {
        int indexOf = path.indexOf("src/");
        if (indexOf != -1) {
            return path.substring(0, path.indexOf("src/"));
        }
        return "";
    }

}
