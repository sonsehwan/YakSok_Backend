package com.sehwan.YakSok.chat.controller;


import com.sehwan.YakSok.chat.dto.ChatMessageDto;
import com.sehwan.YakSok.chat.dto.request.ChatRoomRequest;
import com.sehwan.YakSok.chat.dto.request.DeleteChattingRoomRequest;
import com.sehwan.YakSok.chat.dto.request.FriendChatRoomRequest;
import com.sehwan.YakSok.chat.dto.response.ChatRoomListDto;
import com.sehwan.YakSok.chat.dto.response.ChatRoomResponse;
import com.sehwan.YakSok.chat.service.ChatService;
import com.sehwan.YakSok.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat/room")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatService chatService;

    // 로그인한 유저가 참여 중인 채팅방 목록
    @GetMapping
    public ResponseEntity<ApiResponse<List<ChatRoomListDto>>> getMyRooms(
            @AuthenticationPrincipal Long userId
    ){
        try{
            List<ChatRoomListDto> rooms = chatService.getMyChatRooms(userId);
            return ResponseEntity.ok(ApiResponse.success("채팅방 목록 조회 성공", rooms));
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "채팅방 목록 조회 실패", e.getMessage()));
        }
    }

    // 약국 사장의 상담 채팅방 목록 (개인 채팅과 조회 축이 달라 경로를 나눈다)
    @GetMapping("/consult")
    public ResponseEntity<ApiResponse<List<ChatRoomListDto>>> getConsultRooms(
            @AuthenticationPrincipal Long userId
    ){
        try{
            List<ChatRoomListDto> rooms = chatService.getConsultRooms(userId);
            return ResponseEntity.ok(ApiResponse.success("상담 채팅방 목록 조회 성공", rooms));
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "상담 채팅방 목록 조회 실패", e.getMessage()));
        }
    }

    // 앱에서 "채팅하기" 버튼을 눌렀을 때 호출하는 API
    @PostMapping
    public ResponseEntity<ApiResponse<ChatRoomResponse>> enterChatRoom(@RequestBody ChatRoomRequest request,
                                                                        @AuthenticationPrincipal Long userId) {
        try {
            ChatRoomResponse response = chatService.getOrCreateRoom(request, userId);
            return ResponseEntity.ok(ApiResponse.success("채팅방 연결에 성공하였습니다.",response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "CHAT_ROOM_FAIL", e.getMessage()));
        }
    }

    // 친구와 채팅방 생성
    @PostMapping("/friend")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> enterFriendChatRoom(
            @RequestBody FriendChatRoomRequest request,
            @AuthenticationPrincipal Long userId
    ){
        try{
            ChatRoomResponse response = chatService.getOrCreateFriendRoom(request, userId);
            return ResponseEntity.ok(ApiResponse.success("채팅방 연결에 성공하였습니다.", response));
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "채팅방 연결 실패", e.getMessage()));
        }
    }

    // 채팅 메시지 리스트 반환
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageDto>>> getPreviousMessages(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long roomId) {
        List<ChatMessageDto> messages = chatService.getChatMessages(userId, roomId);
        return ResponseEntity.ok(ApiResponse.success("과거 메시지 조회 성공", messages));
    }

    @PostMapping("/{roomId}/read")
    public ResponseEntity<ApiResponse<Long>> markRead(
            @AuthenticationPrincipal Long userId, @PathVariable Long roomId) {
        Long lastReadMessageId = chatService.markRead(userId, roomId);
        return ResponseEntity.ok(ApiResponse.success("읽음 처리 성공", lastReadMessageId));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<List<ChatRoomListDto>>> deleteRoom(
            @ModelAttribute DeleteChattingRoomRequest request,
            @AuthenticationPrincipal Long userId)
    {
        try{
            Long roomId = request.getRoomId();
            List<ChatRoomListDto> list = chatService.deleteChattingRoom(userId, roomId);

            return ResponseEntity.ok(ApiResponse.success("채팅방 삭제 성공",list));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "채팅방 삭제 실패", e.getMessage()));
        }
    }
}
