package com.sehwan.YakSok.user.controller;

import com.sehwan.YakSok.common.response.ApiResponse;
import com.sehwan.YakSok.user.dto.request.FriendRequestAnswerDto;
import com.sehwan.YakSok.user.dto.request.FriendRequestCreateDto;
import com.sehwan.YakSok.user.dto.response.FriendListDto;
import com.sehwan.YakSok.user.dto.response.ReceivedFriendRequestDto;
import com.sehwan.YakSok.user.service.FriendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/friend")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;


    /**
     *  친구 요청 관련 API
     */
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Void>> createFriendRequest(@RequestBody FriendRequestCreateDto request){
        try{
            log.info("친구 요청 생성 시작");

            friendService.createFriendRequest(request.getUserId(), request.getFriendId());

            return ResponseEntity.ok(ApiResponse.success("친구요청에 성공하였습니다."));
        }catch(Exception e){
            log.error("친구 요청 실패", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "친구요청 실패", e.getMessage()));
        }
    }

    @PatchMapping("/request/{requestId}/answer")
    public ResponseEntity<ApiResponse<Void>> answerFriendRequest(
            @PathVariable Long requestId,
            @RequestBody FriendRequestAnswerDto request){
        ResponseEntity<ApiResponse<Void>> response = null;
        try{
            log.info("친구요청 응답 전달 받음");

            if(request.getAnswer()){
                friendService.acceptFriendRequest(requestId,request.getLoginUserId());

                response = ResponseEntity.ok(ApiResponse.success(requestId +"의 친구 요청을 승락하였습니다."));
            } else {
                friendService.rejectFriendRequest(requestId,request.getLoginUserId());

                response = ResponseEntity.ok(ApiResponse.success(requestId +"의 친구 요청을 거절하였습니다."));
            }
        }catch(Exception e){
            String message = "친구요청 응답에 실패\n" + e.getMessage();
            log.error("친구 요청 응답 실패", e);
            response =  ResponseEntity.badRequest().body(ApiResponse.error(400, "친구요청 응답 실패", message));
        }
        return response;
    }

    @GetMapping("/request/received")
    public ResponseEntity<ApiResponse<List<ReceivedFriendRequestDto>>>  getReceivedFriendRequests(@RequestParam Long loginUserId){
        ResponseEntity<ApiResponse<List<ReceivedFriendRequestDto>>> response = null;

        try {
            log.info("받은 친구 요청 목록을 가져옵니다.");

            List<ReceivedFriendRequestDto> requests = friendService.getReceivedFriendRequests(loginUserId);

            response = ResponseEntity.ok(ApiResponse.success("성공적으로 받은 친구 요청 목록을 가져왔습니다.", requests));
        }catch(Exception e){
            log.error("받은 친구 요청 목록 가져오기 실패", e);
            response = ResponseEntity.badRequest().body(ApiResponse.error(400, "받은 친구 목록 가져오기 실패", e.getMessage()));
        }
        return response;
    }

    /**
     *  친구 관계 관련 API
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<FriendListDto>> getFriendList(@RequestParam Long loginUserId){
        ResponseEntity<ApiResponse<FriendListDto>> response = null;
        try{
            log.info("친구 리스트를 가져옵니다.");

            FriendListDto list = friendService.getFriendList(loginUserId);

            response = ResponseEntity.ok(ApiResponse.success("성공적으로 친구 리스트를 가져왔습니다.", list));
        } catch (Exception e) {
            log.error("친구 리스트 가져오기 실패", e);
            response =  ResponseEntity.badRequest().body(ApiResponse.error(400, "친구 리스트 가져오기 실패", e.getMessage()));
        }

        return response;
    }
}
