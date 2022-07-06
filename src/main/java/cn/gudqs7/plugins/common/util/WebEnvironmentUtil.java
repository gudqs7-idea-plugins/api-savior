package cn.gudqs7.plugins.common.util;

import cn.gudqs7.plugins.common.enums.PluginSettingEnum;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

/**
 * @author wq
 * @date 2022/6/3
 */
public class WebEnvironmentUtil {

    public static String ip = null;

    public static String getIp() {
        if (ip != null) {
            return ip;
        }
        try {
            String defaultIp = PluginSettingHelper.getConfigItem(PluginSettingEnum.DEFAULT_IP);
            if (StringUtils.isNotBlank(defaultIp)) {
                ip = defaultIp;
                return ip;
            }
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface next = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = next.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    String hostAddress0 = inetAddress.getHostAddress();
                    if (inetAddress.isLoopbackAddress() || hostAddress0.contains(":")) {
                        continue;
                    }
                    if (inetAddress.isSiteLocalAddress()) {
                        hostAddress = hostAddress0;
                        break;
                    }
                }
            }
            ip = hostAddress;
            return hostAddress;
        } catch (UnknownHostException | SocketException ignored) {
        }
        ip = "127.0.0.1";
        return "127.0.0.1";
    }

    public static void emptyIp() {
        ip = null;
    }

    /**
     * 获取 Spring Boot 项目配置文件中的网络端口
     *
     * @param project        项目
     * @param containingFile 与此文件同 src 的配置文件优先
     * @return 网络端口
     */
    public static String getPortByConfigFile(Project project, PsiFile containingFile) {
        String nameYml = "application.yml";
        String portByYmlFile = getPortByYamlFile(nameYml, project, containingFile);
        if (StringUtils.isNotBlank(portByYmlFile)) {
            return portByYmlFile;
        }
        String nameYaml = "application.yaml";
        String portByYamlFile = getPortByYamlFile(nameYaml, project, containingFile);
        if (StringUtils.isNotBlank(portByYamlFile)) {
            return portByYamlFile;
        }
        String portByPropertiesFile = getPortByPropertiesFile(project, containingFile);
        if (StringUtils.isNotBlank(portByPropertiesFile)) {
            return portByPropertiesFile;
        }
        return null;
    }

    private static String getPortByPropertiesFile(Project project, PsiFile containingFile) {
        PsiFile[] filesByName = FilenameIndex.getFilesByName(project, "application.properties", GlobalSearchScope.projectScope(project));
        if (filesByName.length > 0) {
            String backPort = null;
            for (PsiFile psiFile : filesByName) {
                String text = psiFile.getText();
                try {
                    Properties properties = new Properties();
                    VirtualFile virtualFile = psiFile.getVirtualFile();
                    properties.load(virtualFile.getInputStream());
                    String port = properties.getProperty("server.port");
                    if (StringUtils.isNotBlank(port)) {
                        if (containingFile != null) {
                            String path = containingFile.getVirtualFile().getPath();
                            String configFilePath = virtualFile.getPath();
                            String projectBasePath1 = getProjectBasePath(path);
                            String projectBasePath2 = getProjectBasePath(configFilePath);
                            if (projectBasePath1.equals(projectBasePath2)) {
                                return port;
                            }
                        }
                        if (backPort == null) {
                            backPort = port;
                        }
                    }
                } catch (Exception ignored) {
                }
            }
            return backPort;
        }
        return null;
    }

    private static String getPortByYamlFile(String name, Project project, PsiFile containingFile) {
        PsiFile[] filesByName = FilenameIndex.getFilesByName(project, name, GlobalSearchScope.projectScope(project));
        if (filesByName.length > 0) {
            String backPort = null;
            for (PsiFile psiFile : filesByName) {
                String text = psiFile.getText();
                try {
                    Yaml yaml = new Yaml();
                    Map<String, Object> map = yaml.load(text);
                    if (map != null && map.size() > 0) {
                        Object serverObj = map.get("server");
                        if (serverObj instanceof Map) {
                            Map server = (Map) serverObj;
                            Object portObj = server.get("port");
                            if (portObj != null) {
                                String port = portObj.toString();
                                if (containingFile != null) {
                                    String path = containingFile.getVirtualFile().getPath();
                                    String projectBasePath1 = getProjectBasePath(path);
                                    String configFilePath = psiFile.getVirtualFile().getPath();
                                    String projectBasePath2 = getProjectBasePath(configFilePath);
                                    if (projectBasePath1.equals(projectBasePath2)) {
                                        return port;
                                    }
                                }
                                if (backPort == null) {
                                    backPort = port;
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }
            return backPort;
        }
        return null;
    }

    private static String getProjectBasePath(String path) {
        int indexOf = path.indexOf("src/");
        if (indexOf != -1) {
            return path.substring(0, path.indexOf("src/"));
        }
        return "";
    }

}
