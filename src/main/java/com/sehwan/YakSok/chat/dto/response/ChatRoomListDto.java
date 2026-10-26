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
    private Long myParticipantId;   // 목록에서 방을 열 때 메시지 전송에 쓸 내 참여자 id
    private String roomName;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private int unreadCount;        // 내가 보낸 것을 제외한, 책갈피 이후 메시지 수
}
