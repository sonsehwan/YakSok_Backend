package com.sehwan.YakSok.chat.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DeleteChattingRoomRequest {
    private Long userId;
    private Long roomId;
}
