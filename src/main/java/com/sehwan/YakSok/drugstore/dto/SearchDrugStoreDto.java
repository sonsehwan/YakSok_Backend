package com.sehwan.YakSok.drugstore.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sehwan.YakSok.drugstore.entity.DrugStore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor //Jackson은 기본 생성자로 객체를 생성한 후 Setter로 매핑을 시작한다
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchDrugStoreDto {

    private String hpid; // 약국 id
    private String dutyName; // 약국 이름
    private String dutyAddr; // 약국 주소
    private String dutyTel1; // 약국 전화번호
}
