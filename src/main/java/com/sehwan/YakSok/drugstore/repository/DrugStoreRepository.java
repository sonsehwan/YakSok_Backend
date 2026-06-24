package com.sehwan.YakSok.drugstore.repository;

import com.sehwan.YakSok.drugstore.entity.DrugStore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DrugStoreRepository extends JpaRepository<DrugStore, Long> {
    boolean existsByHpid(String hpid);

    Optional<DrugStore> findByHpid(String hpid);
}
