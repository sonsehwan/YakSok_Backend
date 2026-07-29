package com.sehwan.YakSok.yaksok.repository;

import com.sehwan.YakSok.yaksok.entity.Yaksok;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface YaksokRepository extends JpaRepository<Yaksok, Long> {

    @Query("SELECT y FROM Yaksok y " +
            "WHERE y.user.email = :email " +
            "ORDER BY y.startDate ASC")
    List<Yaksok> findAllByUserEmail(String email);

    @Query("SELECT y FROM Yaksok y " +
            "LEFT JOIN FETCH y.pills " +
            "JOIN FETCH y.user " +
            "WHERE y.id = :yaksokId")
    Optional<Yaksok> findByIdWithPills(@Param("yaksokId") Long yaksokId);
}
