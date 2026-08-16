package com.sehwan.YakSok.chat.repository;

import com.sehwan.YakSok.chat.entity.ChattingRoom;
import com.sehwan.YakSok.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChattingRoomRepository extends JpaRepository<ChattingRoom, Long> {

    @Query("SELECT  c FROM  ChattingRoom  c JOIN  c.participants p1 JOIN c.participants p2 WHERE p1.user = :user1 AND p2.user = :user2")
    Optional<ChattingRoom> findExistingRoom(@Param("user1") User user1, @Param("user2") User user2);

    // 내가 참여 중인 방을 참여자, 참여자의 유저, 약국 정보까지 한 번에 가져온다.
    // 서브쿼리로 방을 먼저 고르고 바깥에서 fetch 하기 때문에 참여자 목록이 잘리지 않는다.
    @Query("SELECT DISTINCT c FROM ChattingRoom c " +
            "JOIN FETCH c.participants p " +
            "LEFT JOIN FETCH p.user u " +
            "LEFT JOIN FETCH u.myDrugStore " +
            "WHERE c.id IN (" +
            "   SELECT cp.chattingRoom.id FROM ChatParticipant cp WHERE cp.user.id = :userId" +
            ")")
    List<ChattingRoom> findAllWithParticipantsByUserId(@Param("userId") Long userId);

    // 내가 참여 중인 방 번호만 조회: 약속 공유 검증에 사용
    @Query("SELECT cp.chattingRoom.id FROM ChatParticipant cp WHERE cp.user.id = :userId")
    List<Long> findRoomIdByUserId(@Param("userId") Long userId);
}
