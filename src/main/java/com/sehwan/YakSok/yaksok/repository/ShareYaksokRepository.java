package com.sehwan.YakSok.yaksok.repository;

import com.sehwan.YakSok.yaksok.entity.ShareYaksok;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShareYaksokRepository extends JpaRepository<ShareYaksok,Long> {

    // 받는 사람과 약속으로 데이터가 존재하는지 확인
    boolean existsByReceiverIdAndYaksokId(Long receiverId, Long yaksokId);

    // 받는 사람과 약속으로 단일 공유약속 조회
    Optional<ShareYaksok> findByReceiverIdAndYaksokId(Long receiverId, Long yaksokId);

    //
    @Query("SELECT DISTINCT s FROM ShareYaksok s " +
            "JOIN FETCH s.yaksok y " +
            "JOIN FETCH y.user " +
            "LEFT JOIN FETCH y.pills " +
            "WHERE s.receiver.email = :email " +
            "ORDER BY s.savedAt DESC")
    List<ShareYaksok> findAllByReceiverEmail(@Param("email") String email);
}
