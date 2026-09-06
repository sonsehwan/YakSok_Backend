package com.sehwan.YakSok.common.exception;

import com.sehwan.YakSok.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleExternalApi(ExternalApiException e) {
        log.error("외부 API 통신 실패: {}", e.getMessage(), e);
        return ResponseEntity
                .status(ExternalApiException.STATUS)
                .body(ApiResponse.error(
                        ExternalApiException.STATUS,
                        ExternalApiException.CODE,
                        ExternalApiException.CLIENT_MESSAGE));
    }
}
