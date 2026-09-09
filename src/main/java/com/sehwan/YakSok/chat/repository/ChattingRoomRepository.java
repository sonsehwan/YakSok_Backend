package com.sehwan.YakSok.chat.repository;

import com.sehwan.YakSok.chat.entity.ChattingRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChattingRoomRepository extends JpaRepository<ChattingRoom, Long> {

    // 친구 1:1 방: 두 참여자가 모두 USER이고 각각 user id가 일치
    @Query("SELECT c FROM ChattingRoom c " +
            "JOIN c.participants p1 " +
            "JOIN c.participants p2 " +
            "WHERE c.roomType = com.sehwan.YakSok.chat.entity.RoomType.DIRECT " +
            "  AND p1.user.id = :userId1 " +
            "  AND p2.user.id = :userId2")
    Optional<ChattingRoom> findDirectRoom(@Param("userId1") Long userId1,
                                          @Param("userId2") Long userId2);

    // 약국 상담방: 한쪽은 고객(USER), 다른 한쪽은 해당 약국(DRUGSTORE)
    @Query("SELECT c FROM ChattingRoom c " +
            "JOIN c.participants pu " +
            "JOIN c.participants pd " +
            "WHERE c.roomType = com.sehwan.YakSok.chat.entity.RoomType.PHARMACY_CONSULT " +
            "  AND pu.user.id = :customerUserId " +
            "  AND pd.drugStore.hpid = :hpid")
    Optional<ChattingRoom> findConsultRoom(@Param("customerUserId") Long customerUserId,
                                           @Param("hpid") String hpid);

    // 내가 '유저 자격'으로 참여 중인 방 (친구방 + 내가 고객인 상담방).
    // 참여자 렌더링에서 약국명을 쓰므로 p.drugStore 까지 fetch 해야 N+1이 안 난다.
    @Query("SELECT DISTINCT c FROM ChattingRoom c " +
            "JOIN FETCH c.participants p " +
            "LEFT JOIN FETCH p.user " +
            "LEFT JOIN FETCH p.drugStore " +
            "WHERE c.id IN (" +
            "   SELECT cp.chattingRoom.id FROM ChatParticipant cp " +
            "   WHERE cp.user.id = :userId AND cp.hidden = false" +
            ")")
    List<ChattingRoom> findAllAsUserByUserId(@Param("userId") Long userId);

    // 약국 사장의 상담함: 참여자에 그 약국이 있는 방
    @Query("SELECT DISTINCT c FROM ChattingRoom c " +
            "JOIN FETCH c.participants p " +
            "LEFT JOIN FETCH p.user " +
            "LEFT JOIN FETCH p.drugStore " +
            "WHERE c.id IN (" +
            "   SELECT cp.chattingRoom.id FROM ChatParticipant cp " +
            "   WHERE cp.drugStore.hpid = :hpid AND cp.hidden = false" +
            ")")
    List<ChattingRoom> findAllConsultByDrugStoreHpid(@Param("hpid") String hpid);

    // 약속 공유 검증: 내가 유저 자격으로 참여한 방 번호 (ShareYaksokService)
    @Query("SELECT cp.chattingRoom.id FROM ChatParticipant cp WHERE cp.user.id = :userId")
    List<Long> findRoomIdByUserId(@Param("userId") Long userId);
}
