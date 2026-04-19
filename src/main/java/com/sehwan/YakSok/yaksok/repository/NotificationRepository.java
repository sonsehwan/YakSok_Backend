package com.sehwan.YakSok.yaksok.repository;

import com.sehwan.YakSok.yaksok.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification,String> {
}
