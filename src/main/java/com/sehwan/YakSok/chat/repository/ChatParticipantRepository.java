package com.sehwan.YakSok.chat.repository;

import com.sehwan.YakSok.chat.entity.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    Optional<ChatParticipant> findByChattingRoomIdAndUserId(Long roomId, Long userId);

    List<ChatParticipant> findByChattingRoomIdAndHiddenTrue(Long roomId);
}
