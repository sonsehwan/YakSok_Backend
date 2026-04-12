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
    private String date;

    @NotNull
    private String name;

    @NotNull
    private List<PillRequest> pills;
}
