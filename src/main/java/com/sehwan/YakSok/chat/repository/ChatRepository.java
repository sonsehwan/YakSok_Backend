package com.sehwan.YakSok.chat.repository;

import com.sehwan.YakSok.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByRoomIdOrderByCreatedAtAsc(Long roomId);

    List<ChatMessage> findByRoomIdAndCreatedAtAfterOrderByCreatedAtAsc(Long roomId, LocalDateTime after);

    @Query("SELECT m FROM ChatMessage m WHERE m.id IN (" +
            "   SELECT MAX(m2.id) FROM ChatMessage m2 WHERE m2.roomId IN :roomIds GROUP BY m2.roomId" +
            ")")
    List<ChatMessage> findLastMessagesByRoomIds(@Param("roomIds") List<Long> roomIds);

    // 해당 약속이 내가 속한 방들에 실제로 공유된 적이 있는지 센다. 공유 검증 2단계.
    @Query("SELECT COUNT(m) FROM ChatMessage m " +
            "WHERE m.yaksokId = :yaksokId " +
            "AND m.type = com.sehwan.YakSok.chat.entity.MessageType.SHARE_YAKSOK " +
            "AND m.roomId IN :roomIds")
    long countSharedInRooms(@Param("yaksokId") Long yaksokId, @Param("roomIds") List<Long> roomIds);

    // 방과 함께 메시지도 삭제한다 (참여자 0명이 된 방 정리). 파생 삭제는 행마다 select+delete 하므로 벌크 쿼리로.
    @Modifying
    @Query("DELETE FROM ChatMessage m WHERE m.roomId = :roomId")
    void deleteAllByRoomId(@Param("roomId") Long roomId);

    @Query("select max(m.id) from ChatMessage m where m.roomId = :roomId")
    Long findMaxIdByRoomId(@Param("roomId") Long roomId);

    @Query("""
        SELECT m.roomId, COUNT(m)
        FROM ChatMessage m, ChatParticipant p
        WHERE p.id IN :participantIds
          AND m.roomId = p.chattingRoom.id
          AND m.id > COALESCE(p.lastReadMessageId, 0)
          AND m.senderParticipantId <> p.id
        GROUP BY m.roomId
        """)
    List<Object[]> countUnreadByParticipantIds(@Param("participantIds") List<Long> participantIds);
}
