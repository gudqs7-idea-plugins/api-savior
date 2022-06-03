package cn.gudqs7.plugins.common.util.jetbrain;

import com.intellij.notification.*;

/**
 * 通知提示工具类
 *
 * @author wenquan
 * @date 2022/6/2
 */
public class NotificationUtil {

    private static final String NOTIFICATION_TIP_GROUP_ID = "Search Everywhere Api Tip";
    private static final String NOTIFICATION_ERROR_GROUP_ID = "Search Everywhere Api Error";

    public static void showTips(String content) {
        showNotification(NOTIFICATION_TIP_GROUP_ID, content, NotificationType.INFORMATION);
    }

    public static void showError(String content) {
        showNotification(NOTIFICATION_ERROR_GROUP_ID, content, NotificationType.ERROR);
    }

    private static void showNotification(String groupId, String content, NotificationType notificationType) {
        NotificationGroup notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup(groupId);
        Notification notification = notificationGroup.createNotification(content, notificationType);
        Notifications.Bus.notify(notification);
    }

}
