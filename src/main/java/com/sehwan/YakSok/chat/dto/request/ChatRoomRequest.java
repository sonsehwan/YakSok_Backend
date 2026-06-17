package com.sehwan.YakSok.chat.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatRoomRequest {
    private String userEmail;
    private String hpid;
}
