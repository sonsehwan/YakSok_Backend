package com.sehwan.YakSok.chat.controller;


import com.sehwan.YakSok.chat.dto.ChatMessageDto;
import com.sehwan.YakSok.chat.dto.request.ChatRoomRequest;
import com.sehwan.YakSok.chat.dto.response.ChatRoomResponse;
import com.sehwan.YakSok.chat.service.ChatService;
import com.sehwan.YakSok.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat/room")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatService chatService;

    // 앱에서 "채팅하기" 버튼을 눌렀을 때 호출하는 API
    @PostMapping
    public ResponseEntity<ApiResponse<ChatRoomResponse>> enterChatRoom(@RequestBody ChatRoomRequest request) {
        try {
            ChatRoomResponse response = chatService.getOrCreateRoom(request);
            return ResponseEntity.ok(ApiResponse.success("채팅방 연결에 성공하였습니다.",response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "CHAT_ROOM_FAIL", e.getMessage()));
        }
    }

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageDto>>> getPreviousMessages(@PathVariable String roomId) {
        List<ChatMessageDto> messages = chatService.getChatMessages(roomId);
        return ResponseEntity.ok(ApiResponse.success("과거 메시지 조회 성공", messages));
    }
}