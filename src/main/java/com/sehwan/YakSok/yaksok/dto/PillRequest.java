package com.sehwan.YakSok.yaksok.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PillRequest {


    @NotNull
    private String image;

    @NotNull
    private String name;

    @NotNull
    private int dailyFrequency;

    @NotNull
    private String dosage;
}
