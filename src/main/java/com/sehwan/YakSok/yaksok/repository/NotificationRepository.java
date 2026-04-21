package com.sehwan.YakSok.yaksok.repository;

import com.sehwan.YakSok.yaksok.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification,String> {
    List<Notification> findAllByOrderByTimeAscTitleAsc();
}
