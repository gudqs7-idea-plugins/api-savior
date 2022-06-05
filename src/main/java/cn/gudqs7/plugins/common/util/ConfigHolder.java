package cn.gudqs7.plugins.common.util;

import cn.gudqs7.plugins.common.util.structure.BaseTypeParseUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import lombok.SneakyThrows;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wq
 */
public class ConfigHolder {

    private static final String CONFIG_FILE_PATH = "docer-config.properties";

    private static final Map<String, String> CONFIG = new ConcurrentHashMap<>(16);

    public static void putConfig(Map<String, String> config) {
        if (config != null) {
            CONFIG.putAll(config);
        }
    }

    public static void removeConfig() {
        CONFIG.clear();
    }

    public static Map<String, String> getConfig() {
        return CONFIG;
    }

    public static boolean configExists() {
        return CONFIG.isEmpty();
    }

    public static String getConfigItem(String key) {
        return CONFIG.get(key);
    }

    public static String getConfigItem(String key, String defaultVal) {
        String configItem = getConfigItem(key);
        if (configItem != null) {
            return configItem;
        }
        return defaultVal;
    }

    public static Boolean getConfigItemBool(String key) {
        return getConfigItemBool(key, false);
    }

    public static Boolean getConfigItemBool(String key, Boolean defaultVal) {
        String configItem = getConfigItem(key);
        if (configItem != null) {
            return BaseTypeParseUtil.parseBoolean(configItem, defaultVal);
        }
        return defaultVal;
    }

    public static Integer getConfigItemInt(String key) {
        String configItem = getConfigItem(key);
        if (configItem != null) {
            return BaseTypeParseUtil.parseInt(configItem);
        }
        return null;
    }

    /**
     * 初始化配置信息
     *
     * @param project            项目
     * @param currentVirtualFile 与此文件同一个 src 下的优先
     */
    public static void initConfig(Project project, VirtualFile currentVirtualFile) {
        Map<String, String> config = getConfig(project, currentVirtualFile);
        putConfig(config);
    }

    /**
     * 根据默认的配置文件获取配置信息
     *
     * @param project            项目
     * @param currentVirtualFile 与此文件同一个 src 下的优先
     * @return 配置信息
     */
    @SneakyThrows
    public static Map<String, String> getConfig(Project project, VirtualFile currentVirtualFile) {
        String defaultConfigPath = project.getBasePath() + File.separator + CONFIG_FILE_PATH;
        VirtualFile virtualFileByDefault = LocalFileSystem.getInstance().findFileByPath(defaultConfigPath);
        if (virtualFileByDefault != null) {
            Properties properties = new Properties();
            properties.load(virtualFileByDefault.getInputStream());
            return toMap(properties);
        }
        PsiFile[] filesByName = FilenameIndex.getFilesByName(project, CONFIG_FILE_PATH, GlobalSearchScope.projectScope(project));
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
