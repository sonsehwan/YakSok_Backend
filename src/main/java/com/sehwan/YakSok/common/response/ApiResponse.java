package com.sehwan.YakSok.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiResponse<T> {

    private boolean success;
    private int status;
    private String code;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public ApiResponse(boolean success, int status, String code, String message, T data) {
        this.success = success;
        this.status = status;
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    // 성공 응답(반환할 데이터가 있는 경우)
    public static <T> ApiResponse<T> success(String message,T data) {
        return new ApiResponse<>(true, 200, "SUCCESS", message, data);
    }

    // 성공 응답 (데이터가 없는 경우, 예: 수정/삭제 완료)
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, 200, "SUCCESS", message, null);
    }

    // 실패/에러 응답 (기본 에러)
    public static <T> ApiResponse<T> error(int status, String code, String message) {
        return new ApiResponse<>(false, status, code, message, null);
    }

    // 실패/에러 응답 (상세 에러 데이터가 필요한 경우, 예: 입력값 검증 실패 목록)
    public static <T> ApiResponse<T> error(int status, String code, String message, T data) {
        return new ApiResponse<>(false, status, code, message, data);
    }
}
