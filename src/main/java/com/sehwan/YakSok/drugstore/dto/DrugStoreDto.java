package com.sehwan.YakSok.drugstore.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sehwan.YakSok.drugstore.entity.DrugStore;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor //Jackson은 기본 생성자로 객체를 생성한 후 Setter로 매핑을 시작한다
@JsonIgnoreProperties(ignoreUnknown = true)
public class DrugStoreDto {

    private String dutyName; // 약국 이름
    private String dutyAddr; // 약국 주소
    private String dutyTel1; // 약국 전화번호
    private String startTime; // 약국 영업 시작
    private String endTime; // 약국 영업 종료

    private String latitude; // 약국 위도
    private String longitude; // 약국 경도

    public DrugStore toEntity() {
        return DrugStore.builder()
                .dutyName(this.dutyName)
                .dutyAddr(this.dutyAddr)
                .dutyTel1(this.dutyTel1)
                .startTime(this.startTime)
                .endTime(this.endTime)
                .latitude(this.latitude)
                .longitude(this.longitude)
                .build();
    }
}
