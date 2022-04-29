package cn.gudqs7.plugins.docer.util;

import com.intellij.notification.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * @author wq
 */
public class ActionUtil {

    private static final String NOTIFICATION_GROUP_ID = "notificationGroup";
    public static boolean show = true;
    public static String ip = null;

    public static void showNotification(String content) {
        showNotification(content, NotificationDisplayType.BALLOON, NotificationType.INFORMATION);
    }

    public static void showNotification(String content, NotificationDisplayType notificationDisplayType, NotificationType notificationType) {
        NotificationGroup notificationGroup = new NotificationGroup(NOTIFICATION_GROUP_ID,
                notificationDisplayType, true);
        Notification notification = notificationGroup.createNotification(content,
                notificationType);
        Notifications.Bus.notify(notification);
    }

//    public static void showNotificationNew(String content, NotificationType notificationType) {
//        NotificationGroup notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup("Docer Savior Notification Group");
//        Notification notification = notificationGroup.createNotification(content,
//                notificationType);
//        Notifications.Bus.notify(notification);
//    }


    public static PsiClass getPsiClass(PsiElement psiElement) {
        PsiClass psiClass = null;
        if (psiElement instanceof PsiClass) {
            psiClass = (PsiClass) psiElement;
        }
        return psiClass;
    }

    public static PsiMethod getPsiMethod(PsiElement psiElement) {
        PsiMethod psiMethod = null;
        if (psiElement instanceof PsiMethod) {
            psiMethod = (PsiMethod) psiElement;
        }
        return psiMethod;
    }

    public static PsiDirectory getPsiDirectory(PsiElement psiElement) {
        PsiDirectory psiDirectory = null;
        if (psiElement instanceof PsiDirectory) {
            psiDirectory = (PsiDirectory) psiElement;
        }
        return psiDirectory;
    }

    public static void showDialog(Project project, String tip, String content) {
        showDialog(project, tip, content, false);
    }

    public static void showDialog(Project project, String tip, String content, boolean force) {
        ActionUtil.showNotification("可以粘贴(Ctrl+V)了");
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

    public static String getIp() {
        if (ip != null) {
            return ip;
        }
        try {
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

    public static void handleException(Throwable e1) {
        e1.printStackTrace();
        String message = e1.getMessage();
        if (StringUtils.isBlank(message)) {
            message = e1.toString();
        }
        ActionUtil.showNotification("插件运行失败: " + message, NotificationDisplayType.BALLOON, NotificationType.ERROR);
    }

}
