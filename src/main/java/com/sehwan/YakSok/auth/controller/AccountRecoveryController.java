package com.sehwan.YakSok.auth.controller;

import com.sehwan.YakSok.auth.dto.request.FindIdSendCodeRequest;
import com.sehwan.YakSok.auth.dto.request.FindIdVerifyRequest;
import com.sehwan.YakSok.auth.dto.request.ResetPwSendCodeRequest;
import com.sehwan.YakSok.auth.dto.request.ResetPwVerifyRequest;
import com.sehwan.YakSok.auth.dto.response.FindIdResponse;
import com.sehwan.YakSok.auth.service.AccountRecoveryService;
import com.sehwan.YakSok.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AccountRecoveryController {

    private final AccountRecoveryService accountRecoveryService;

    @PostMapping("/find-id/send-code")
    public ResponseEntity<ApiResponse<Void>> sendFindIdCode(@Valid @RequestBody FindIdSendCodeRequest dto) {
        try {
            accountRecoveryService.sendFindIdCode(dto.getEmail());
            return ResponseEntity.ok(ApiResponse.success("인증코드를 이메일로 발송했습니다."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "FIND_ID_FAIL", e.getMessage()));
        }
    }

    @PostMapping("/find-id/verify")
    public ResponseEntity<ApiResponse<FindIdResponse>> verifyFindId(@Valid @RequestBody FindIdVerifyRequest dto) {
        try {
            String loginId = accountRecoveryService.verifyFindId(dto.getEmail(), dto.getCode());
            return ResponseEntity.ok(ApiResponse.success("아이디 찾기 성공", new FindIdResponse(loginId)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "FIND_ID_FAIL", e.getMessage()));
        }
    }

    @PostMapping("/reset-password/send-code")
    public ResponseEntity<ApiResponse<Void>> sendResetPwCode(@Valid @RequestBody ResetPwSendCodeRequest dto) {
        try {
            accountRecoveryService.sendResetPwCode(dto.getLoginId(), dto.getEmail());
            return ResponseEntity.ok(ApiResponse.success("인증코드를 이메일로 발송했습니다."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "RESET_PW_FAIL", e.getMessage()));
        }
    }

    @PostMapping("/reset-password/verify")
    public ResponseEntity<ApiResponse<Void>> verifyResetPw(@Valid @RequestBody ResetPwVerifyRequest dto) {
        try {
            accountRecoveryService.verifyResetPw(dto.getLoginId(), dto.getEmail(), dto.getCode());
            return ResponseEntity.ok(ApiResponse.success("임시 비밀번호를 이메일로 발송했습니다."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "RESET_PW_FAIL", e.getMessage()));
        }
    }
}
