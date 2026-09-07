package com.sehwan.YakSok.user.dto.request;


import lombok.*;

@Getter
@NoArgsConstructor
public class LoginRequest {
    private String loginId;
    private String password;
}
