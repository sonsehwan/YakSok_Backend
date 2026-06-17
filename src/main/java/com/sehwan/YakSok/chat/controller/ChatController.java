package com.sehwan.YakSok.chat.controller;

import com.sehwan.YakSok.chat.dto.ChatMessageDto;
import com.sehwan.YakSok.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate template;

    @MessageMapping("/chat/message")
    public void sendMessage(ChatMessageDto message){
        chatService.saveMessage(message);

        template.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
    }
}
