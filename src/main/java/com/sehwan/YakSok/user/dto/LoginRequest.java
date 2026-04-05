package com.sehwan.YakSok.user.dto;


import lombok.*;

@Getter
@NoArgsConstructor
public class LoginRequest {
    private String email;
    private String password;
}
