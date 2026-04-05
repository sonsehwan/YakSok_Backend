package com.sehwan.YakSok.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ModifyInfoRequest {
    private String email;
    private String nickname;
}
