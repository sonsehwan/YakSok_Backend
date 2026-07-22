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
    private String message;
    private MessageType type;
    private Long yaksokId;

    // 엔티티 -> DTO
    public static ChatMessageDto from(ChatMessage entity) {
        return new ChatMessageDto(
                entity.getRoomId(),
                entity.getSender(),
                entity.getMessage(),
                entity.getType(),
                entity.getYaksokId()
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
