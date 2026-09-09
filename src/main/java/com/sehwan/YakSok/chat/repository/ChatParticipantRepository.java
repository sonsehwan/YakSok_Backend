package com.sehwan.YakSok.chat.repository;

import com.sehwan.YakSok.chat.entity.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    Optional<ChatParticipant> findByChattingRoomIdAndUserId(Long roomId, Long userId);

    // 약국 사장이 상담방을 열 때: 사장 자신이 아니라 약국이 참여자다.
    Optional<ChatParticipant> findByChattingRoomIdAndDrugStore_Hpid(Long roomId, String hpid);

    List<ChatParticipant> findByChattingRoomIdAndHiddenTrue(Long roomId);

    // 발신자 검증 + 이름 스냅샷용. user / drugStore를 함께 fetch 해서 displayName()이 LAZY 터지지 않게 한다.
    @Query("SELECT p FROM ChatParticipant p " +
            "LEFT JOIN FETCH p.user " +
            "LEFT JOIN FETCH p.drugStore " +
            "WHERE p.id = :id AND p.chattingRoom.id = :roomId")
    Optional<ChatParticipant> findWithRefsByIdAndRoomId(@Param("id") Long id, @Param("roomId") Long roomId);

    // 회원 탈퇴 정리용
    List<ChatParticipant> findByUserId(Long userId);

    long countByChattingRoomId(Long roomId);
}
