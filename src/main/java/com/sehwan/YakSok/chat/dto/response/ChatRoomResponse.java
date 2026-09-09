package com.sehwan.YakSok.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponse {
    private Long roomId;            // 소켓 구독(/sub/chat/room/{roomId})에 사용할 방 번호
    private Long myParticipantId;   // 이 방에서 나를 대표하는 참여자 id — 메시지 보낼 때 이 값을 싣는다
    private boolean isNew;          // 방이 새로 만들어졌는지, 기존 방인지 확인용
}
