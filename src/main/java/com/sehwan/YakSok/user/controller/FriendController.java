package com.sehwan.YakSok.user.controller;

import com.sehwan.YakSok.common.response.ApiResponse;
import com.sehwan.YakSok.user.dto.request.FriendRequestAnswerDto;
import com.sehwan.YakSok.user.dto.request.FriendRequestCreateDto;
import com.sehwan.YakSok.user.dto.response.FriendListDto;
import com.sehwan.YakSok.user.dto.response.FriendQrCodeDto;
import com.sehwan.YakSok.user.dto.response.ReceivedFriendRequestDto;
import com.sehwan.YakSok.user.dto.response.UserProfileDto;
import com.sehwan.YakSok.user.dto.response.UserSearchResultDto;
import com.sehwan.YakSok.user.service.FriendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<UserSearchResultDto>> searchUser(
            @RequestParam String email,
            @AuthenticationPrincipal Long userId){

        try{
            UserSearchResultDto user = friendService.searchUserByEmail(email, userId);

            return ResponseEntity.ok(ApiResponse.success("사용자를 찾았습니다.", user));
        }catch(Exception e){
            log.error("사용자 검색 실패", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "사용자 검색 실패", e.getMessage()));
        }
    }

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Void>> createFriendRequest(@RequestBody FriendRequestCreateDto request,
                                                                  @AuthenticationPrincipal Long userId){
        try{
            log.info("친구 요청 생성 시작");

            friendService.createFriendRequest(userId, request.getFriendId());

            return ResponseEntity.ok(ApiResponse.success("친구요청에 성공하였습니다."));
        }catch(Exception e){
            log.error("친구 요청 실패", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "친구요청 실패", e.getMessage()));
        }
    }

    @PatchMapping("/request/{requestId}/answer")
    public ResponseEntity<ApiResponse<Void>> answerFriendRequest(
            @PathVariable Long requestId,
            @RequestBody FriendRequestAnswerDto request,
            @AuthenticationPrincipal Long userId){
        ResponseEntity<ApiResponse<Void>> response = null;
        try{
            log.info("친구요청 응답 전달 받음");

            if(request.getAnswer()){
                friendService.acceptFriendRequest(requestId, userId);

                response = ResponseEntity.ok(ApiResponse.success(requestId +"의 친구 요청을 승락하였습니다."));
            } else {
                friendService.rejectFriendRequest(requestId, userId);

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
    public ResponseEntity<ApiResponse<List<ReceivedFriendRequestDto>>>  getReceivedFriendRequests(@AuthenticationPrincipal Long userId){
        ResponseEntity<ApiResponse<List<ReceivedFriendRequestDto>>> response = null;

        try {
            log.info("받은 친구 요청 목록을 가져옵니다.");

            List<ReceivedFriendRequestDto> requests = friendService.getReceivedFriendRequests(userId);

            response = ResponseEntity.ok(ApiResponse.success("성공적으로 받은 친구 요청 목록을 가져왔습니다.", requests));
        }catch(Exception e){
            log.error("받은 친구 요청 목록 가져오기 실패", e);
            response = ResponseEntity.badRequest().body(ApiResponse.error(400, "받은 친구 목록 가져오기 실패", e.getMessage()));
        }
        return response;
    }

    // 내 QR 코드 발급/재발급 ("내 QR 보여주기" 화면을 열 때마다 호출, 10분 후 만료)
    @PostMapping("/qr-code")
    public ResponseEntity<ApiResponse<FriendQrCodeDto>> issueQrCode(@AuthenticationPrincipal Long userId){
        try{
            log.info("QR 코드를 발급합니다.");

            FriendQrCodeDto qrCode = friendService.issueQrCode(userId);

            return ResponseEntity.ok(ApiResponse.success("QR 코드를 발급했습니다.", qrCode));
        }catch(Exception e){
            log.error("QR 코드 발급 실패", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "QR 코드 발급 실패", e.getMessage()));
        }
    }

    // QR 코드로 알아낸 사용자의 프로필 + 나와의 관계 상태 조회
    @GetMapping("/profile/{code}")
    public ResponseEntity<ApiResponse<UserProfileDto>> getUserProfile(
            @PathVariable String code,
            @AuthenticationPrincipal Long loginUserId){
        try{
            log.info("QR 코드로 사용자 프로필을 조회합니다.");

            UserProfileDto profile = friendService.getUserProfileByQrCode(code, loginUserId);

            return ResponseEntity.ok(ApiResponse.success("프로필을 조회했습니다.", profile));
        }catch(Exception e){
            log.error("프로필 조회 실패", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "프로필 조회 실패", e.getMessage()));
        }
    }

    /**
     *  친구 관계 관련 API
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<FriendListDto>> getFriendList(@AuthenticationPrincipal Long userId){
        ResponseEntity<ApiResponse<FriendListDto>> response = null;
        try{
            log.info("친구 리스트를 가져옵니다.");

            FriendListDto list = friendService.getFriendList(userId);

            response = ResponseEntity.ok(ApiResponse.success("성공적으로 친구 리스트를 가져왔습니다.", list));
        } catch (Exception e) {
            log.error("친구 리스트 가져오기 실패", e);
            response =  ResponseEntity.badRequest().body(ApiResponse.error(400, "친구 리스트 가져오기 실패", e.getMessage()));
        }

        return response;
    }

    // 친구 삭제 (내 목록에서만 지우고, 갱신된 친구 목록을 돌려준다.)
    @DeleteMapping
    public ResponseEntity<ApiResponse<FriendListDto>> deleteFriend(
            @AuthenticationPrincipal Long userId,
            @RequestParam Long friendId){
        ResponseEntity<ApiResponse<FriendListDto>> response = null;
        try{
            log.info("친구를 삭제합니다.");

            FriendListDto list = friendService.deleteFriend(userId, friendId);

            response = ResponseEntity.ok(ApiResponse.success("친구를 삭제하였습니다.", list));
        } catch (Exception e) {
            log.error("친구 삭제 실패", e);
            response =  ResponseEntity.badRequest().body(ApiResponse.error(400, "친구 삭제 실패", e.getMessage()));
        }

        return response;
    }
}
