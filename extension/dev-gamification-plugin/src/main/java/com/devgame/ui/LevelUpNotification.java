package com.devgame.ui;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;

public class LevelUpNotification {
    
    public static void show(Project project, int newLevel) {
        String title = "🎉 Level Up!";
        String content = "Congratulations! You've reached Level " + newLevel + "!";
        
        Notification notification = new Notification(
            "DevGame",
            title,
            content,
            NotificationType.INFORMATION
        );
        
        Notifications.Bus.notify(notification, project);
    }
}
