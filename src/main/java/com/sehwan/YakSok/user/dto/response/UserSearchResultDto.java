package com.sehwan.YakSok.user.dto.response;

import com.sehwan.YakSok.user.entity.User;
import lombok.Getter;

@Getter
public class UserSearchResultDto {
    private final Long userId;
    private final String nickname;
    private final String email;

    private UserSearchResultDto(Long userId, String nickname, String email) {
        this.userId = userId;
        this.nickname = nickname;
        this.email = email;
    }

    public static UserSearchResultDto from(User user){
        return new UserSearchResultDto(
                user.getId(),
                user.getNickname(),
                user.getEmail());
    }
}
