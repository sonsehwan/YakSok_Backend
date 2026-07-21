package com.sehwan.YakSok.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestAnswer {
    private Long requestId;
    private Long loginUserId;
    private String answer;
}
