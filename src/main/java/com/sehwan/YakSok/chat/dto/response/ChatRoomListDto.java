package com.sehwan.YakSok.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.A;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomListDto {
    private Long roomId;
    private String roomName;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
}
