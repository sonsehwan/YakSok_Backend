package com.sehwan.YakSok.yaksok.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.List;


@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class YaksokRequest {

    @NotNull
    private String email;

    @NotNull
    private String title;

    @NotNull
    private String startDate;

    @NotNull
    private int prescriptionDays;

    private boolean takeMorning;
    private boolean takeLunch;
    private boolean takeDinner;

    private String timeMorning;
    private String timeLunch;
    private String timeDinner;

    @NotNull
    private String dosageTime;

    @NotNull
    private List<PillRequest> pills;

    @NotNull
    private String status;
}
