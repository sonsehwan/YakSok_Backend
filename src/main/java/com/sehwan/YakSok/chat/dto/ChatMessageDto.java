package com.sehwan.YakSok.chat.dto;

import com.sehwan.YakSok.chat.entity.ChatMessage;
import com.sehwan.YakSok.drugstore.entity.DrugStore;
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

    public ChatMessage toEntity() {
        return ChatMessage.builder()
                .roomId(this.roomId)
                .sender(this.sender)
                .message(this.message)
                .build();
    }
}
