package com.sehwan.YakSok.user.dto;


import lombok.*;

@Getter
@NoArgsConstructor
public class LoginRequestDto {
    private String email;
    private String password;
}
