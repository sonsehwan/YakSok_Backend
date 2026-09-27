package com.sehwan.YakSok.yaksok.dto;

import com.sehwan.YakSok.yaksok.entity.Yaksok;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SaveYaksokResponse {
    Yaksok yaksok;
}
