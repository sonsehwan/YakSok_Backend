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
    private String roomId;
    private String sender;
    private String senderNickname;
    private String message;
    private MessageType type;
    private Long yaksokId;
    private String createdAt;      // Gson이 java.time을 못 다루므로 문자열로 내린다

    // 엔티티 -> DTO
    public static ChatMessageDto from(ChatMessage entity, String senderNickname) {
        return new ChatMessageDto(
                entity.getRoomId(),
                entity.getSender(),
                senderNickname,
                entity.getMessage(),
                entity.getType(),
                entity.getYaksokId(),
                entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null
        );
    }

    public ChatMessage toEntity() {
        return ChatMessage.builder()
                .roomId(this.roomId)
                .sender(this.sender)
                .message(this.message)
                // 구버전 앱이 type 없이 보내면 TEXT로 취급한다.
                .type(this.type != null ? this.type : MessageType.TEXT)
                .yaksokId(this.yaksokId)
                .build();
    }
}
