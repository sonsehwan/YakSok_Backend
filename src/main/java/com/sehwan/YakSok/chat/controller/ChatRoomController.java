package com.sehwan.YakSok.chat.controller;


import com.sehwan.YakSok.chat.dto.request.ChatRoomRequest;
import com.sehwan.YakSok.chat.dto.response.ChatRoomResponse;
import com.sehwan.YakSok.chat.service.ChatService;
import com.sehwan.YakSok.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}