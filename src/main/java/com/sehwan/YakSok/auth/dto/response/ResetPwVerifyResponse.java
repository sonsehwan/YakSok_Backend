package com.sehwan.YakSok.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 비밀번호 재설정 - 인증코드 검증 성공 응답. 이 토큰으로 새 비밀번호를 확정한다. */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ResetPwVerifyResponse {
    private String resetToken;
}
