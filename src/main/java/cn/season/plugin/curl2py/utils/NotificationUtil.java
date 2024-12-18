package cn.season.plugin.curl2py.utils;

import cn.season.plugin.curl2py.Curl2pyPlugin;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

/**
 * the NotificationUtil
 *
 * @author PanLongfei
 * @date 2024-11-24
 */
public class NotificationUtil {

    private static final NotificationGroup GROUP = NotificationGroupManager.getInstance()
            .getNotificationGroup("Curl2pyPluginGroup");

    public static void warning(String content, Project project) {
        GROUP.createNotification(content, NotificationType.WARNING)
                .setTitle(Curl2pyPlugin.PLUGIN_ID_STR)
                .notify(project);
    }

    public static void error(String content, Project project) {
        GROUP.createNotification(content, NotificationType.ERROR)
                .setTitle(Curl2pyPlugin.PLUGIN_ID_STR)
                // .addAction(DumbAwareAction.create(HeadersBundle.message("issues.feedback"),
                //         e -> BrowserUtil.open(Website.ISSUES)))
                .notify(project);
    }

    public static void info(String content, Project project) {
        GROUP.createNotification(content, NotificationType.INFORMATION)
                .setTitle(Curl2pyPlugin.PLUGIN_ID_STR)
                .notify(project);
    }

}
