package com.sehwan.YakSok.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ResetPwConfirmRequest {

    @NotBlank(message = "잘못된 요청입니다.")
    private String resetToken;

    @NotBlank(message = "새 비밀번호를 입력해주세요.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+])[A-Za-z\\d!@#$%^&*()_+]{8,16}$",
            message = "비밀번호는 8~16자의 영문·숫자·특수문자를 모두 포함해야 합니다.")
    private String newPassword;
}
