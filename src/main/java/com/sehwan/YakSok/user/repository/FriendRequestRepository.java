package com.sehwan.YakSok.user.repository;

import com.sehwan.YakSok.user.entity.FriendRequest;
import com.sehwan.YakSok.user.entity.FriendRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    // 내가 받은 친구 요청 목록 가져오기 (요청 보낸 사람 정보까지 함께 조회)
    @Query("SELECT fr FROM FriendRequest fr JOIN FETCH fr.requester " +
            "WHERE fr.receiver.id = :receiverId AND fr.status = :status " +
            "ORDER BY fr.requestedAt DESC")
    List<FriendRequest> findReceivedRequests(@Param("receiverId") Long receiverId,
                                             @Param("status") FriendRequestStatus status);
}
