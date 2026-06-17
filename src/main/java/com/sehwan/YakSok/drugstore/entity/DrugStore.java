package com.sehwan.YakSok.drugstore.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "drug_store")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DrugStore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String dutyName; // 약국 이름

    private String dutyAddr; // 약국 주소
    private String dutyTel1; // 전화번호
    private String startTime; // 영업 시작
    private String endTime; // 영업 종료
    private String latitude; // 위도
    private String longitude; // 경도
}