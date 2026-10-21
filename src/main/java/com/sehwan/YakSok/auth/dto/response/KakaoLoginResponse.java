package com.sehwan.YakSok.auth.dto.response;

import com.sehwan.YakSok.user.dto.response.LoginResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoLoginResponse {

    private boolean newUser;
    private LoginResponse login;   // 기존 회원일 때만
    private String nickname;       // 신규 회원일 때, 가입 화면에 미리 채울 카카오 닉네임(없을 수 있음)
    private String email;          // 신규 회원일 때, 가입 화면에 보여 줄 카카오 이메일(표시용, 가입 요청에는 쓰지 않는다)
}
