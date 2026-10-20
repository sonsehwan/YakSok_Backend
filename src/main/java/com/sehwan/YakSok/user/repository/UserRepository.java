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

    Optional<User> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);

    Optional<User> findBySocialTypeAndSocialId(String socialType, String socialId);

    boolean existsByMyDrugStore_HpidAndIdNot(String hpid, Long id);

    boolean existsByMyDrugStore_Hpid(String hpid);
}
