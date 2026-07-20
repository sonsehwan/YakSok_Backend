package com.sehwan.YakSok.user.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FirebaseTokenRequest {
    private String fcmToken;
}
