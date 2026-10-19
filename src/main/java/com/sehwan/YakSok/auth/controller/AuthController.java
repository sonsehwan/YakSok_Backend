package com.sehwan.YakSok.auth.controller;

import com.sehwan.YakSok.auth.dto.request.KakaoLoginRequest;
import com.sehwan.YakSok.auth.dto.request.KakaoSignUpRequest;
import com.sehwan.YakSok.auth.dto.request.RefreshTokenRequest;
import com.sehwan.YakSok.auth.dto.response.KakaoLoginResponse;
import com.sehwan.YakSok.auth.dto.response.TokenResponse;
import com.sehwan.YakSok.common.response.ApiResponse;
import com.sehwan.YakSok.user.dto.response.LoginResponse;
import com.sehwan.YakSok.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(@RequestBody RefreshTokenRequest dto) {
        try{
            TokenResponse newAccessToken = userService.reissueAccessToken(dto.getRefreshToken());
            return ResponseEntity.ok(ApiResponse.success("토큰 재발급 성공", newAccessToken));
        }catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "REFRESH_FAIL", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody RefreshTokenRequest dto) {
        userService.logout(dto.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("로그아웃 성공"));
    }

    // 실패는 401이 아니라 400으로 준다 — 401은 앱의 토큰 재발급 로직(TokenAuthenticator)을 건드린다
    @PostMapping("/kakao")
    public ResponseEntity<ApiResponse<KakaoLoginResponse>> kakaoLogin(@Valid @RequestBody KakaoLoginRequest dto) {
        try {
            return ResponseEntity.ok(ApiResponse.success("카카오 인증 성공", userService.kakaoLogin(dto)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "KAKAO_LOGIN_FAIL", e.getMessage()));
        }
    }

    @PostMapping("/kakao/signup")
    public ResponseEntity<ApiResponse<LoginResponse>> kakaoSignUp(@Valid @RequestBody KakaoSignUpRequest dto) {
        try {
            return ResponseEntity.ok(ApiResponse.success("회원가입에 성공하였습니다.", userService.kakaoSignUp(dto)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "KAKAO_SIGNUP_FAIL", e.getMessage()));
        }
    }
}
