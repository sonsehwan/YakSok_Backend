package com.sehwan.YakSok.user.repository;

import com.sehwan.YakSok.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    //Id로 사용자 찾기
    Optional<User> findById(Long id);

    // 이메일로 사용자 찾기
    Optional<User> findByEmail(String email);

    // 이메일 중복 여부 확인
    boolean existsByEmail(String email);

    // 닉네임 중복 여부 확인 메서드 추가
    boolean existsByNickname(String nickname);

    // hpid로 약사 유저 찾기
    Optional<User> findByMyDrugStore_Hpid(String hpid);

    boolean existsByMyDrugStore_HpidAndEmailNot(String hpid, String email);

    Optional<User> findByNickname(String nickname);

    // 여러 이메일의 사용자를 한 번에 조회 (채팅 메시지의 닉네임 매핑용)
    List<User> findByEmailIn(Collection<String> emails);
}
