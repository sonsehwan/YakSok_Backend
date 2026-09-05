package com.sehwan.YakSok.auth.controller;

import com.sehwan.YakSok.auth.dto.request.RefreshTokenRequest;
import com.sehwan.YakSok.auth.dto.response.TokenResponse;
import com.sehwan.YakSok.common.response.ApiResponse;
import com.sehwan.YakSok.user.service.UserService;
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
}
