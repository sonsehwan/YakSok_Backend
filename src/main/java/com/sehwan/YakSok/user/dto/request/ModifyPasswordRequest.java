package com.sehwan.YakSok.user.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ModifyPasswordRequest {
    private Long userId;
    private String currentPassword;
    private String newPassword;
}
