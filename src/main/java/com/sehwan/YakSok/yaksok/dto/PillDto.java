package com.sehwan.YakSok.yaksok.dto;

import com.sehwan.YakSok.yaksok.entity.Pill;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PillDto {
    private Long id;
    private String name;
    private String image;
    private int dailyFrequency; // 1일 투여 횟수
    private String dosage;      // 1회 투약량

    public static PillDto from(Pill pill) {
        return new PillDto(
                pill.getId(),
                pill.getName(),
                pill.getImage(),
                pill.getDailyFrequency(),
                pill.getDosage()
        );
    }
}