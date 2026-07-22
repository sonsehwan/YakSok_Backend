package com.sehwan.YakSok.user.dto.request;

import lombok.Getter;

@Getter
public class FriendRequestCreateDto {
    private Long userId;
    private Long friendId;
}
