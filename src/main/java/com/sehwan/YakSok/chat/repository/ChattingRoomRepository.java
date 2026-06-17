package com.sehwan.YakSok.chat.repository;

import com.sehwan.YakSok.chat.entity.ChattingRoom;
import com.sehwan.YakSok.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChattingRoomRepository extends JpaRepository<ChattingRoom, Long> {

    @Query("SELECT  c FROM  ChattingRoom  c JOIN  c.participants p1 JOIN c.participants p2 WHERE p1.user = :user1 AND p2.user = :user2")
    Optional<ChattingRoom> findExistingRoom(@Param("user1") User user1, @Param("user2") User user2);
}
