package com.sbadss.service;

import com.sbadss.entity.Notification;
import com.sbadss.entity.User;

public interface NotificationService {
    void createNotification(String title, String message, Notification.NotificationType type, User recipient);
    void sendNotification(Long userId, String title, String message, Notification.NotificationType type);
    void sendBroadcast(String title, String message, Notification.NotificationType type);
    void markAllAsRead(Long userId);
    void markAsRead(Long id);
    java.util.List<Notification> getMyNotifications(Long userId);
    long getUnreadCount(Long userId);
}
