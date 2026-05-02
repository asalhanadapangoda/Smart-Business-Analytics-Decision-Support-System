package com.sbadss.service.impl;

import com.sbadss.entity.Notification;
import com.sbadss.entity.User;
import com.sbadss.repository.NotificationRepository;
import com.sbadss.repository.UserRepository;
import com.sbadss.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public void sendNotification(Long userId, String title, String message, Notification.NotificationType type) {
        User recipient = userRepository.findById(userId).orElse(null);
        if (recipient == null) return;

        Notification notification = Notification.builder()
                .recipient(recipient)
                .title(title)
                .message(message)
                .type(type)
                .build();
        
        notificationRepository.save(notification);

        // Send via WebSocket
        messagingTemplate.convertAndSendToUser(
                recipient.getUsername(),
                "/queue/notifications",
                notification
        );
        log.info("Notification sent to user {}: {}", recipient.getUsername(), title);
    }

    @Override
    @Transactional
    public void sendBroadcast(String title, String message, Notification.NotificationType type) {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            sendNotification(user.getId(), title, message, type);
        }
        log.info("Broadcast notification sent: {}", title);
    }

    @Override
    public List<Notification> getMyNotifications(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }
}
