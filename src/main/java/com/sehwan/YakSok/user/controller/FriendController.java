package com.sehwan.YakSok.user.controller;

import com.sehwan.YakSok.common.response.ApiResponse;
import com.sehwan.YakSok.user.service.FriendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/friend")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Void>> createFriendRequest(Long userId, Long friendId){
        try{

            log.info("친구 요청 생성 시작");

            friendService.createFriendRequest(userId,friendId);

            return ResponseEntity.ok(ApiResponse.success("친구요청에 성공하였습니다."));
        }catch(Exception e){
            log.error("친구 요청 실패", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "친구요청 실패", e.getMessage()));
        }
    }
}
