package com.sehwan.YakSok.yaksok.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;


@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class YaksokRequest {

    @NotNull
    private String title;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private int prescriptionDays;

    private boolean takeMorning;
    private boolean takeLunch;
    private boolean takeDinner;

    @NotNull
    private List<PillRequest> pills;
}
