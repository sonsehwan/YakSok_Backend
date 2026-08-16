package com.sehwan.YakSok.yaksok.repository;

import com.sehwan.YakSok.yaksok.entity.Notification;
import com.sehwan.YakSok.yaksok.entity.Yaksok;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification,Long> {
    @Query("SELECT n FROM Notification n JOIN n.yaksok y " +
            "WHERE y.user.id = :userId " +
            "ORDER BY n.time ASC, n.title ASC")
    List<Notification> findAllByUserId(@Param("userId") Long userId);


    List<Notification> findByDateAndTimeAndIsTakenFalse(String date, String time);
}
