package cn.gudqs7.plugins.common.util;

import cn.gudqs7.plugins.common.enums.PluginSettingEnum;
import cn.gudqs7.plugins.common.util.structure.BaseTypeParseUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wq
 */
public class PluginSettingHelper {

    private static final String CONFIG_FILE_PATH = "docer-config.properties";
    private static final Map<String, String> CONFIG = new ConcurrentHashMap<>(16);

    private static VirtualFile configFile;

    /**
     * 将配置保存到缓存
     *
     * @param config 配置
     */
    public static void saveConfigToCache(Map<String, String> config) {
        if (config != null) {
            CONFIG.putAll(config);
        }
    }

    /**
     * 清除配置缓存
     */
    public static void clearConfigCache() {
        CONFIG.clear();
    }

    /**
     * 配置是否存在
     *
     * @return boolean
     */
    public static boolean configExists() {
        return !CONFIG.isEmpty();
    }

    /**
     * 配置是否不存在
     *
     * @return boolean
     */
    public static boolean configNotExists() {
        return !configExists();
    }

    /**
     * 获取配置项
     *
     * @param pluginSettingEnum 插件设置枚举
     * @return {@link T}
     */
    public static <T> T getConfigItem(PluginSettingEnum pluginSettingEnum) {
        return getConfigItem(pluginSettingEnum, null);
    }

    /**
     * 获取配置项
     *
     * @param pluginSettingEnum 插件设置枚举
     * @param defaultVal        默认值
     * @return {@link T}
     */
    public static <T> T getConfigItem(PluginSettingEnum pluginSettingEnum, T defaultVal) {
        if (pluginSettingEnum == null) {
            return defaultVal;
        }
        String settingKey = pluginSettingEnum.getSettingKey();
        switch (pluginSettingEnum.getType()) {
            case BOOL:
                boolean defaultBool = false;
                if (defaultVal instanceof Boolean) {
                    defaultBool = (boolean) defaultVal;
                }
                return (T) getConfigItemBool(settingKey, defaultBool);
            case STRING:
                String defaultStr = null;
                if (defaultVal instanceof String) {
                    defaultStr = (String) defaultVal;
                }
                return (T) getConfigItem(settingKey, defaultStr);
            case INTEGER:
                Integer defaultInt = null;
                if (defaultVal instanceof Integer) {
                    defaultInt = (Integer) defaultVal;
                }
                return (T) getConfigItemInt(settingKey, defaultInt);
            default:
                return defaultVal;
        }
    }

    /**
     * 获取配置项
     *
     * @param key 关键
     * @return {@link String}
     */
    public static String getConfigItem(String key) {
        return getConfigItem(key, null);
    }

    /**
     * 获取配置项
     *
     * @param key        关键
     * @param defaultVal 默认值
     * @return {@link String}
     */
    public static String getConfigItem(String key, String defaultVal) {
        if (configNotExists()) {
            return defaultVal;
        }
        return CONFIG.getOrDefault(key, defaultVal);
    }

    /**
     * 获取配置项bool
     *
     * @param key 关键
     * @return boolean
     */
    public static boolean getConfigItemBool(String key) {
        return getConfigItemBool(key, false);
    }

    /**
     * 获取配置项bool
     *
     * @param key        关键
     * @param defaultVal 默认值
     * @return boolean
     */
    public static Boolean getConfigItemBool(String key, boolean defaultVal) {
        String configItem = getConfigItem(key);
        if (configItem != null) {
            return BaseTypeParseUtil.parseBoolean(configItem, defaultVal);
        }
        return defaultVal;
    }

    /**
     * 获取配置项int
     *
     * @param key 关键
     * @return {@link Integer}
     */
    public static Integer getConfigItemInt(String key) {
        return getConfigItemInt(key, null);
    }

    /**
     * 获取配置项int
     *
     * @param key        关键
     * @param defaultVal 默认值
     * @return {@link Integer}
     */
    public static Integer getConfigItemInt(String key, Integer defaultVal) {
        String configItem = getConfigItem(key);
        if (configItem != null) {
            return BaseTypeParseUtil.parseInt(configItem, defaultVal);
        }
        return defaultVal;
    }

    // region init config

    /**
     * 初始化配置信息
     *
     * @param project            项目
     * @param currentVirtualFile 与此文件同一个 src 下的优先
     */
    public static void initConfig(Project project, VirtualFile currentVirtualFile) {
        Map<String, String> config = getConfigFromFile(project, currentVirtualFile);
        saveConfigToCache(config);
    }

    /**
     * 根据默认的配置文件获取配置信息
     *
     * @param project            项目
     * @param currentVirtualFile 与此文件同一个 src 下的优先
     * @return 配置信息
     */
    @SneakyThrows
    public static Map<String, String> getConfigFromFile(Project project, VirtualFile currentVirtualFile) {
        if (configFile != null && configFile.exists()) {
            return toMap();
        }
        String defaultConfigPath = project.getBasePath() + File.separator + CONFIG_FILE_PATH;
        VirtualFile virtualFileByDefault = LocalFileSystem.getInstance().findFileByPath(defaultConfigPath);
        if (virtualFileByDefault != null) {
            configFile = virtualFileByDefault;
            return toMap();
        }
        PsiFile[] filesByName = FilenameIndex.getFilesByName(project, CONFIG_FILE_PATH, GlobalSearchScope.projectScope(project));
        if (filesByName.length > 0) {
            VirtualFile back = null;
            for (PsiFile psiFile : filesByName) {
                VirtualFile virtualFile = psiFile.getVirtualFile();

                String path = currentVirtualFile.getPath();
                String configFilePath = virtualFile.getPath();
                String projectBasePath1 = getProjectBasePath(path);
                String projectBasePath2 = getProjectBasePath(configFilePath);
                if (projectBasePath1.equals(projectBasePath2)) {
                    configFile = virtualFile;
                    return toMap();
                }
                if (back == null) {
                    back = virtualFile;
                }
            }
            configFile = back;
            return toMap();
        }
        return null;
    }

    private static Map<String, String> toMap() throws IOException {
        Properties properties = new Properties();
        properties.load(configFile.getInputStream());
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

    // endregion init config
}
