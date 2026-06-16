package com.sehwan.YakSok.chat.repository;

import com.sehwan.YakSok.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<ChatMessage, Long> {
}
