package com.sehwan.YakSok.yaksok.controller;


import com.sehwan.YakSok.common.response.ApiResponse;
import com.sehwan.YakSok.yaksok.dto.SaveYaksokResponse;
import com.sehwan.YakSok.yaksok.dto.YaksokRequest;
import com.sehwan.YakSok.yaksok.entity.Yaksok;
import com.sehwan.YakSok.yaksok.service.YaksokService;
import com.sehwan.YakSok.yaksok.dto.SaveYaksokResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/yaksok")
@RequiredArgsConstructor
public class YaksokController {
    private final YaksokService yaksokService;

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
}
