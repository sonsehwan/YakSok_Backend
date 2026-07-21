package com.sehwan.YakSok.user.controller;

import com.sehwan.YakSok.common.response.ApiResponse;
import com.sehwan.YakSok.user.dto.request.FriendRequestAnswer;
import com.sehwan.YakSok.user.dto.response.FriendListDto;
import com.sehwan.YakSok.user.service.FriendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ApiResponse<Void>> createFriendRequest(@RequestParam Long userId, @RequestParam Long friendId){
        try{
            log.info("친구 요청 생성 시작");

            friendService.createFriendRequest(userId,friendId);

            return ResponseEntity.ok(ApiResponse.success("친구요청에 성공하였습니다."));
        }catch(Exception e){
            log.error("친구 요청 실패", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "친구요청 실패", e.getMessage()));
        }
    }

    @PatchMapping("/response")
    public ResponseEntity<ApiResponse<Void>> responseFriendRequest(@RequestBody FriendRequestAnswer request){
        ResponseEntity<ApiResponse<Void>> response = null;
        try{
            log.info("친구요청 응답 전달 받음");

            if(request.getAnswer() == "accept"){
                friendService.acceptFriendRequest(request.getRequestId(),request.getLoginUserId());

                response = ResponseEntity.ok(ApiResponse.success(request.getRequestId() +"의 친구 요청을 승락하였습니다."));
            } else if (request.getAnswer() == "reject") {
                friendService.rejectFriendRequest(request.getRequestId(),request.getLoginUserId());

                response = ResponseEntity.ok(ApiResponse.success(request.getRequestId() +"의 친구 요청을 거절하였습니다."));
            }
        }catch(Exception e){
            String message = "친구요청 응답에 실패\n" + e.getMessage();
            log.error("친구 요청 응답 실패", e);
            response =  ResponseEntity.badRequest().body(ApiResponse.error(400, "친구요청 응답 실패", message));
        }
        return response;
    }

    /**
     *  친구 관계 관련 API
     */
    @GetMapping("/List")
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
