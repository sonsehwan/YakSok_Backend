package com.sehwan.YakSok.user.repository;

import com.sehwan.YakSok.user.entity.FriendRelation;
import com.sehwan.YakSok.user.entity.FriendRelationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendRelationRepository extends JpaRepository<FriendRelation, FriendRelationId> {
}
