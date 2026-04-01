package com.sehwan.YakSok.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ModifyPasswordRequestDto {
    private String email;
    private String currentPassword;
    private String newPassword;
}
