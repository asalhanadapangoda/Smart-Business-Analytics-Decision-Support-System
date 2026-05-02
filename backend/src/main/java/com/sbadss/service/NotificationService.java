package com.sbadss.service;

import com.sbadss.entity.Notification;
import java.util.List;

public interface NotificationService {
    void sendNotification(Long userId, String title, String message, Notification.NotificationType type);
    void sendBroadcast(String title, String message, Notification.NotificationType type);
    List<Notification> getMyNotifications(Long userId);
    void markAsRead(Long notificationId);
    void markAllAsRead(Long userId);
    long getUnreadCount(Long userId);
}
