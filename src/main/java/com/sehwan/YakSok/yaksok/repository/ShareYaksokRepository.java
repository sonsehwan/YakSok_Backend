package com.sehwan.YakSok.yaksok.repository;

import com.sehwan.YakSok.user.entity.User;
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

    // User에게 약속을 공유한 user들의 목록
    @Query("SELECT DISTINCT  y.user FROM ShareYaksok s JOIN s.yaksok y WHERE  s.receiver.id = :userId")
    List<User> findDistinctBySendersByReceiverId(@Param("userId") Long userId);

    // User가 받은 공유 약속 목록
    @Query("SELECT DISTINCT s FROM ShareYaksok s " +
            "JOIN  FETCH s.yaksok y " +
            "JOIN FETCH y.user " +
            "LEFT JOIN FETCH y.pills " +
            "WHERE s.receiver.id = :userId " +
            "ORDER BY s.savedAt DESC")
    List<ShareYaksok> findAllByReceiverId(@Param("userId") Long userId);

    // 로그인 유저 id와 공유자 id로 약속 목록 가져오기
    @Query("SELECT DISTINCT s FROM ShareYaksok s " +
            "JOIN FETCH s.yaksok y " +
            "JOIN FETCH y.user u " +
            "LEFT JOIN FETCH y.pills " +
            "WHERE s.receiver.id = :userId AND u.id = :senderId " +
            "ORDER BY s.savedAt DESC")
    List<ShareYaksok> findAllByReceiverIdAndSenderId(@Param("userId") Long userId, @Param("senderId") Long senderId);

    @Query("SELECT DISTINCT s FROM ShareYaksok s " +
            "JOIN FETCH s.receiver r " +
            "WHERE s.yaksok.id = :yaksokId")
    List<ShareYaksok> findAllByYaksokId(@Param("yaksokId") Long yaksokId);
}
