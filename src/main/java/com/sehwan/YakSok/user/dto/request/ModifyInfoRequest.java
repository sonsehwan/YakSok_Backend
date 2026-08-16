package com.sehwan.YakSok.user.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ModifyInfoRequest {
    private Long userId;
    private String nickname;
}
