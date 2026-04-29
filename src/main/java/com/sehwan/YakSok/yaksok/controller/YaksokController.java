package com.sehwan.YakSok.yaksok.controller;


import com.sehwan.YakSok.common.response.ApiResponse;
import com.sehwan.YakSok.yaksok.dto.SaveYaksokResponse;
import com.sehwan.YakSok.yaksok.dto.YaksokRequest;
import com.sehwan.YakSok.yaksok.entity.Notification;
import com.sehwan.YakSok.yaksok.entity.Yaksok;
import com.sehwan.YakSok.yaksok.repository.NotificationRepository;
import com.sehwan.YakSok.yaksok.service.YaksokService;
import com.sehwan.YakSok.yaksok.dto.SaveYaksokResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/yaksok")
@RequiredArgsConstructor
public class YaksokController {
    private final YaksokService yaksokService;
    private final NotificationRepository notificationRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<SaveYaksokResponse>> saveYaksok(@RequestBody YaksokRequest request) {
        try{
            SaveYaksokResponse response  = yaksokService.saveYaksok(request);
            return ResponseEntity.ok(ApiResponse.success("약속 저장에 성공하였습니다.", response));
        }catch(Exception e){
            log.error("에러 발생 : {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(400,"FAIL_SAVE_YAKSOK", e.getMessage()));
        }
    }

    @PatchMapping("/notifications/{notificationId}/status")
    public ResponseEntity<ApiResponse<Void>> updateNotificationStatus(
            @PathVariable Long notificationId,
            @RequestParam boolean isTaken) {

        yaksokService.updateNotificationStatus(notificationId, isTaken);
        return ResponseEntity.ok(ApiResponse.success("알림 상태가 성공적으로 변경되었습니다."));
    }

    @GetMapping("{userEmail}/notifications")
    public ResponseEntity<ApiResponse<List<Notification>>> getNotifications(
            @PathVariable String userEmail
            ){
        List<Notification> notifications = notificationRepository.findAllByUserEmail(userEmail);
        return ResponseEntity.ok(ApiResponse.success("성공적으로 알림 리스트를 가져왔습니다.", notifications));
    }

    @GetMapping("/list/{userEmail}")
    public ResponseEntity<ApiResponse<List<Yaksok>>> getYaksokList(@PathVariable String userEmail){
        try {
            List<Yaksok> yaksokList = yaksokService.getYaksokListByUserEmail(userEmail);
            return ResponseEntity.ok(ApiResponse.success("약속 목록을 성공적으로 가져왔습니다.", yaksokList));
        } catch(Exception e) {
            log.error("에러 발생 : {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "FAIL_GET_YAKSOK_LIST", e.getMessage()));
        }
    }
}
