package com.sehwan.YakSok.user.repository;

import com.sehwan.YakSok.user.entity.FriendQrCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FriendQrCodeRepository extends JpaRepository<FriendQrCode, Long> {

    Optional<FriendQrCode> findByCode(String code);
}
