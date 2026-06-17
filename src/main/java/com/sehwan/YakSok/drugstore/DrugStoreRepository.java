package com.sehwan.YakSok.drugstore;

import com.sehwan.YakSok.drugstore.entity.DrugStore;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DrugStoreRepository extends JpaRepository<DrugStore, Long> {
}
