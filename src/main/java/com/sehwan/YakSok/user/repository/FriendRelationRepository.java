package com.sehwan.YakSok.user.repository;

import com.sehwan.YakSok.user.entity.FriendRelation;
import com.sehwan.YakSok.user.entity.FriendRelationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRelationRepository extends JpaRepository<FriendRelation, FriendRelationId> {
    
    // 친구 리스트 가져오기
    @Query("SELECT fr FROM FriendRelation fr JOIN fetch fr.friend WHERE  fr.user.id = :userId")
    List<FriendRelation> findByUserId(@Param("userId") Long userId);
}
