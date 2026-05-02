package com.sbadss.repository;

import com.sbadss.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySessionIdOrderByMessageTimestampAsc(String sessionId);
    List<ChatMessage> findByUserIdOrderByMessageTimestampDesc(Long userId);
}
