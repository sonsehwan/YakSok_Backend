package com.sehwan.YakSok.auth.controller;

import com.sehwan.YakSok.auth.dto.request.*;
import com.sehwan.YakSok.auth.dto.response.KakaoLoginResponse;
import com.sehwan.YakSok.auth.dto.response.SignUpVerifyResponse;
import com.sehwan.YakSok.auth.dto.response.TokenResponse;
import com.sehwan.YakSok.auth.service.SignUpVerificationService;
import com.sehwan.YakSok.common.response.ApiResponse;
import com.sehwan.YakSok.user.dto.response.LoginResponse;
import com.sehwan.YakSok.auth.service.AuthService;
import com.sehwan.YakSok.user.dto.request.LoginRequest;
import com.sehwan.YakSok.user.dto.request.UserRequest;
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

    private final AuthService authService;
    private final SignUpVerificationService signUpVerificationService;

    //회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signUp(@Valid @RequestBody UserRequest dto) {
        try {
            authService.signUp(dto);

            return ResponseEntity.ok(ApiResponse.success("회원가입에 성공하였습니다."));
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "SIGNUP_FAIL", e.getMessage()));
        }
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest dto) {
        try {
            LoginResponse loginUser = authService.login(dto);
            return ResponseEntity.ok(ApiResponse.success("로그인 성공", loginUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "LOGIN_FAIL", e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(@RequestBody RefreshTokenRequest dto) {
        try{
            TokenResponse newAccessToken = authService.reissueAccessToken(dto.getRefreshToken());
            return ResponseEntity.ok(ApiResponse.success("토큰 재발급 성공", newAccessToken));
        }catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "REFRESH_FAIL", e.getMessage()));
        }
    }

    @PostMapping("/signup/send-code")
    public ResponseEntity<ApiResponse<Void>> sendCode(@Valid @RequestBody SignUpSendCodeRequest request) {
        try{
            signUpVerificationService.sendCode(request.getEmail());
            return ResponseEntity.ok(ApiResponse.success("인증코드 전송 성공"));
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "SIGNUP_VERIFY_FAIL", e.getMessage()));
        }
    }

    @PostMapping("/signup/verify")
    public ResponseEntity<ApiResponse<SignUpVerifyResponse>> verifySignUp(@Valid @RequestBody SignUpVerifyRequest dto) {
        try {
            String token = signUpVerificationService.verifyCode(dto.getEmail(), dto.getCode());
            return ResponseEntity.ok(ApiResponse.success("인증 성공", new SignUpVerifyResponse(token)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "SIGNUP_VERIFY_FAIL", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody RefreshTokenRequest dto) {
        authService.logout(dto.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("로그아웃 성공"));
    }

    // 실패는 401이 아니라 400으로 준다 — 401은 앱의 토큰 재발급 로직(TokenAuthenticator)을 건드린다
    @PostMapping("/kakao")
    public ResponseEntity<ApiResponse<KakaoLoginResponse>> kakaoLogin(@Valid @RequestBody KakaoLoginRequest dto) {
        try {
            return ResponseEntity.ok(ApiResponse.success("카카오 인증 성공", authService.kakaoLogin(dto)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "KAKAO_LOGIN_FAIL", e.getMessage()));
        }
    }

    @PostMapping("/kakao/signup")
    public ResponseEntity<ApiResponse<LoginResponse>> kakaoSignUp(@Valid @RequestBody KakaoSignUpRequest dto) {
        try {
            return ResponseEntity.ok(ApiResponse.success("회원가입에 성공하였습니다.", authService.kakaoSignUp(dto)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "KAKAO_SIGNUP_FAIL", e.getMessage()));
        }
    }
}
