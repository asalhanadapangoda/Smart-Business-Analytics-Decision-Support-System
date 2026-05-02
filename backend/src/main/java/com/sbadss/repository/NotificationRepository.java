package com.sbadss.repository;

import com.sbadss.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long userId);
    long countByRecipientIdAndIsReadFalse(Long userId);
}
