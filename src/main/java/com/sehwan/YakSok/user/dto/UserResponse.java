package com.sehwan.YakSok.user.dto;

import com.sehwan.YakSok.user.entity.User;
import lombok.Getter;

@Getter
public class UserResponse {
    private String email;
    private String password;
    private String nickname;

    public UserResponse(User user) {
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.nickname = user.getNickname();
    }
}
