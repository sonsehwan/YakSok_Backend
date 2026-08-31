package com.sehwan.YakSok.user.repository;

import com.sehwan.YakSok.user.entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {

    Optional<UserDevice> findByToken(String token);

    List<UserDevice> findAllByUserId(Long userId);

    void deleteByUserIdAndToken(Long userId, String token);

    void deleteByToken(String token);

    List<UserDevice> findAllByUserIdIn(Collection<Long> userIds);
}