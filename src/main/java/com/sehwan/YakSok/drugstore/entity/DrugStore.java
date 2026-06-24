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
    private String hpid; // 약국id

    @Column(nullable = false)
    private String dutyName; // 약국 이름

    private String dutyAddr; // 약국 주소
    private String dutyTel1; // 전화번호
    private String startTime; // 영업 시작
    private String endTime; // 영업 종료
    private String latitude; // 위도
    private String longitude; // 경도

    //영업 종료 시간(월~일 + 공휴일(8))
    private String dutyTime1c;
    private String dutyTime2c;
    private String dutyTime3c;
    private String dutyTime4c;
    private String dutyTime5c;
    private String dutyTime6c;
    private String dutyTime7c;
    private String dutyTime8c;

    //영업 시작 시간(월~일 + 공휴일(8))
    private String dutyTime1s;
    private String dutyTime2s;
    private String dutyTime3s;
    private String dutyTime4s;
    private String dutyTime5s;
    private String dutyTime6s;
    private String dutyTime7s;
    private String dutyTime8s;

    // 우편번호
    private String postCdn1;
    private String postCdn2;

    private String wgs84Lon; // 경도
    private String wgs84Lat; // 위도
}