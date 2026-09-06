package com.sehwan.YakSok.common.exception;

public class ExternalApiException extends RuntimeException {

    public static final int STATUS = 503;
    public static final String CODE = "EXTERNAL_API_UNAVAILABLE";
    public static final String CLIENT_MESSAGE =
            "공공데이터 서버가 일시적으로 응답하지 않습니다. 잠시 후 다시 시도해 주세요.";

    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
