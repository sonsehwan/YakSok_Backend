package com.sehwan.YakSok.yaksok.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ShareYaksokRequest {
    private String userEmail;   // 저장하는 사람
    private Long yaksokId;      // 공유받은 약속
}
