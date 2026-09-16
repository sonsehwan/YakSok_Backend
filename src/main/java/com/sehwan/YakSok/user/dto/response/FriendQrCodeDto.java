package com.sehwan.YakSok.user.dto.response;

import lombok.Getter;

@Getter
public class FriendQrCodeDto {
    private final String code;

    private FriendQrCodeDto(String code) {
        this.code = code;
    }

    public static FriendQrCodeDto of(String code) {
        return new FriendQrCodeDto(code);
    }
}
