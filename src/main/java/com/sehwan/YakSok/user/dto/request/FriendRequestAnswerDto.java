package com.sehwan.YakSok.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestAnswerDto {
    private Long loginUserId;
    private Boolean answer;
}
