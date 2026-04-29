package com.sehwan.YakSok.yaksok.repository;

import com.sehwan.YakSok.yaksok.entity.Yaksok;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface YaksokRepository extends JpaRepository<Yaksok, Long> {
    List<Yaksok> findAllByUserEmail(String email);
}
