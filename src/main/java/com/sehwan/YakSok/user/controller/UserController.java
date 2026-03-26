package com.sehwan.YakSok.user.controller;

import com.sehwan.YakSok.common.response.ApiResponse;
import com.sehwan.YakSok.user.dto.LoginRequestDto;
import com.sehwan.YakSok.user.dto.UserRequestDto;
import com.sehwan.YakSok.user.dto.UserResponseDto;
import com.sehwan.YakSok.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 안드로이드 앱의 HTTP 요청을 처리하는 컨트롤러
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signUp(@RequestBody UserRequestDto dto) {

        try {
            userService.signUp(dto);

            return ResponseEntity.ok(ApiResponse.success("회원가입에 성공하였습니다."));
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "FAIL", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponseDto>> login(@RequestBody LoginRequestDto dto) {
        System.out.println("로그인 요청 도달!");
        try {
            UserResponseDto loginUser = userService.login(dto);
            return ResponseEntity.ok(ApiResponse.success("로그인 성공", loginUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "LOGIN_FAIL", e.getMessage()));
        }
    }
}