package com.sehwan.YakSok.drugstore.entity;

import com.sehwan.YakSok.drugstore.dto.SearchDrugStoreDto;
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

    public SearchDrugStoreDto toDTO() {
        return SearchDrugStoreDto.builder()
                .hpid(this.hpid)
                .dutyName(this.dutyName)
                .dutyAddr(this.dutyAddr)
                .dutyTel1(this.dutyTel1)
                .dutyTime1c(this.dutyTime1c)
                .dutyTime2c(this.dutyTime2c)
                .dutyTime3c(this.dutyTime3c)
                .dutyTime4c(this.dutyTime4c)
                .dutyTime5c(this.dutyTime5c)
                .dutyTime6c(this.dutyTime6c)
                .dutyTime7c(this.dutyTime7c)
                .dutyTime8c(this.dutyTime8c)
                .dutyTime1s(this.dutyTime1s)
                .dutyTime2s(this.dutyTime2s)
                .dutyTime3s(this.dutyTime3s)
                .dutyTime4s(this.dutyTime4s)
                .dutyTime5s(this.dutyTime5s)
                .dutyTime6s(this.dutyTime6s)
                .dutyTime7s(this.dutyTime7s)
                .dutyTime8s(this.dutyTime8s)
                .postCdn1(this.postCdn1)
                .postCdn2(this.postCdn2)
                .wgs84Lon(this.wgs84Lon)
                .wgs84Lat(this.wgs84Lat)
                .build();
    }
}