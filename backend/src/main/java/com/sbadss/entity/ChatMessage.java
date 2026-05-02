package com.sbadss.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_chat_session", columnList = "session_id"),
        @Index(name = "idx_chat_user", columnList = "user_id")
})
public class ChatMessage extends BaseEntity {

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "user_message", nullable = false, columnDefinition = "TEXT")
    private String userMessage;

    @Column(name = "bot_response", nullable = false, columnDefinition = "TEXT")
    private String botResponse;

    @Column(name = "intent_detected")
    private String intentDetected;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "message_timestamp", nullable = false)
    private LocalDateTime messageTimestamp;

    @PrePersist
    protected void onPersist() {
        if (messageTimestamp == null) {
            messageTimestamp = LocalDateTime.now();
        }
    }
}
