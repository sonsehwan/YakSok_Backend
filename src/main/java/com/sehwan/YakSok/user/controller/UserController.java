package com.sehwan.YakSok.user.controller;

import com.sehwan.YakSok.common.response.ApiResponse;
import com.sehwan.YakSok.user.dto.*;
import com.sehwan.YakSok.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signUp(@RequestBody UserRequest dto) {

        try {
            userService.signUp(dto);

            return ResponseEntity.ok(ApiResponse.success("회원가입에 성공하였습니다."));
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "SIGNUP_FAIL", e.getMessage()));
        }
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(@RequestBody LoginRequest dto) {
        System.out.println("로그인 요청 도달!");
        try {
            UserResponse loginUser = userService.login(dto);
            return ResponseEntity.ok(ApiResponse.success("로그인 성공", loginUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "LOGIN_FAIL", e.getMessage()));
        }
    }

    //내 정보 수정
    @PatchMapping("/info")
    public ResponseEntity<ApiResponse<UserResponse>> modifyInfo(@RequestBody ModifyInfoRequest dto) {
        try{
            UserResponse modifyUser = userService.modifyInfo(dto);
            return ResponseEntity.ok(ApiResponse.success("수정 성공", modifyUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "MODIFY_INFO_FAIL", e.getMessage()));
        }
    }

    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<Void>> modifyPassword(@RequestBody ModifyPasswordRequest dto) {
        try{
            userService.modifyPassword(dto);
            return ResponseEntity.ok(ApiResponse.success("비밀번호 수정 성공"));
        }catch(RuntimeException e){
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "MODIFY_PW_FAIL", e.getMessage()));
        }
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String email) {
        try {
            userService.deleteUser(email);
            return ResponseEntity.ok(ApiResponse.success("회원 탈퇴(삭제)가 성공적으로 처리되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "FAIL_DELETE_USER", e.getMessage()));
        }
    }
}