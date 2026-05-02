package com.sbadss.service.impl;

import com.sbadss.entity.Notification;
import com.sbadss.entity.User;
import com.sbadss.repository.NotificationRepository;
import com.sbadss.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final com.sbadss.repository.UserRepository userRepository;

    @Override
    public void sendBroadcast(String title, String message, Notification.NotificationType type) {
        log.info("Broadcasting notification: {}", title);
        userRepository.findAll().forEach(u -> {
            createNotification(title, message, type, u);
        });
    }

    @Override
    public void createNotification(String title, String message, Notification.NotificationType type, User recipient) {
        log.info("Creating notification: {} for user: {}", title, recipient.getUsername());
        Notification notification = Notification.builder()
                .title(title)
                .message(message)
                .type(type)
                .recipient(recipient)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    public void sendNotification(Long userId, String title, String message, Notification.NotificationType type) {
        log.info("Sending notification to user ID: {}", userId);
        Notification notification = Notification.builder()
                .title(title)
                .message(message)
                .type(type)
                .recipient(com.sbadss.entity.User.builder().id(userId).build())
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(Long userId) {
        log.info("Marking all notifications as read for user ID: {}", userId);
        notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId).forEach(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @Override
    public void markAsRead(Long id) {
        log.info("Marking notification as read: {}", id);
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @Override
    public java.util.List<Notification> getMyNotifications(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }
}
