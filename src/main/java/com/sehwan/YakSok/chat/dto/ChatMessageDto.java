package com.sehwan.YakSok.chat.dto;

import com.sehwan.YakSok.chat.entity.ChatMessage;
import com.sehwan.YakSok.chat.entity.MessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private Long roomId;
    private Long senderParticipantId;  // 클라가 방 입장 시 받은 myParticipantId를 그대로 실어 보낸다
    private String senderNickname;     // 서버가 채워 내려준다 (발신자 이름 스냅샷: 개인=닉네임, 약국=약국명)
    private String message;
    private MessageType type;
    private Long yaksokId;
    private String createdAt;          // Gson이 java.time을 못 다루므로 문자열로 내린다

    // 엔티티 -> DTO. 이름은 엔티티에 스냅샷된 senderName을 그대로 쓴다.
    public static ChatMessageDto from(ChatMessage entity) {
        return new ChatMessageDto(
                entity.getRoomId(),
                entity.getSenderParticipantId(),
                entity.getSenderName(),
                entity.getMessage(),
                entity.getType(),
                entity.getYaksokId(),
                entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null
        );
    }

    // DTO -> 엔티티. senderName은 서비스에서 참여자 검증 후 세팅한다.
    public ChatMessage toEntity() {
        return ChatMessage.builder()
                .roomId(this.roomId)
                .senderParticipantId(this.senderParticipantId)
                .message(this.message)
                // 구버전 앱이 type 없이 보내면 TEXT로 취급한다.
                .type(this.type != null ? this.type : MessageType.TEXT)
                .yaksokId(this.yaksokId)
                .build();
    }
}
