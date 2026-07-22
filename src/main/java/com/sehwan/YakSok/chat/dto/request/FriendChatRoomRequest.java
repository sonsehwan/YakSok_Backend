package com.sehwan.YakSok.chat.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FriendChatRoomRequest {
    private String userEmail;
    private Long friendId;
}