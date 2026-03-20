package com.sehwan.YakSok.user.repository;

import com.sehwan.YakSok.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    // 이메일로 사용자 찾기
    Optional<User> findByEmail(String email);

    // 이메일 중복 여부 확인
    boolean existsByEmail(String email);

    // 닉네임 중복 여부 확인 메서드 추가
    boolean existsByNickname(String nickname);
}
