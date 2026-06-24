package com.sehwan.YakSok.drugstore.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CreateDrugStoreRequest {
    String email;
    SearchDrugStoreDto drugStore;
}
