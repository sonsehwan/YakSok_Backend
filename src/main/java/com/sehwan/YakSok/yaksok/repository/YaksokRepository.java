package com.sehwan.YakSok.yaksok.repository;

import com.sehwan.YakSok.yaksok.entity.Yaksok;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface YaksokRepository extends JpaRepository<Yaksok, Long> {

    @Query("SELECT y FROM Yaksok y " +
            "WHERE y.user.email = :email " +
            "ORDER BY y.startDate ASC")
    List<Yaksok> findAllByUserEmail(String email);
}
