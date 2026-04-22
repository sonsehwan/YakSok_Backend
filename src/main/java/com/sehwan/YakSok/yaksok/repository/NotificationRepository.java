package com.sehwan.YakSok.yaksok.repository;

import com.sehwan.YakSok.yaksok.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification,Long> {
    List<Notification> findAllByOrderByTimeAscTitleAsc();

    @Query("SELECT n FROM Notification n JOIN n.yaksok y " +
            "WHERE y.user.email = :email " +
            "ORDER BY n.time ASC, n.title ASC")
    List<Notification> findAllByUserEmail(@Param("email") String email);
}
