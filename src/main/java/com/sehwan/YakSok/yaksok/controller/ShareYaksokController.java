package com.sehwan.YakSok.yaksok.controller;

import com.sehwan.YakSok.common.response.ApiResponse;
import com.sehwan.YakSok.yaksok.dto.ShareYaksokRequest;
import com.sehwan.YakSok.yaksok.dto.YaksokDto;
import com.sehwan.YakSok.yaksok.service.ShareYaksokService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/shared-yaksok")
@RequiredArgsConstructor
public class ShareYaksokController {

    private final ShareYaksokService shareYaksokService;

    // 채팅에서 공유받은 약속 상세의 "저장" 버튼
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> saveSharedYaksok(@RequestBody ShareYaksokRequest request) {
        try {
            shareYaksokService.saveSharedYaksok(request.getUserEmail(), request.getYaksokId());
            return ResponseEntity.ok(ApiResponse.success("공유 약속을 저장하였습니다."));
        } catch (Exception e) {
            log.error("공유 약속 저장 실패 : {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "FAIL_SAVE_SHARED_YAKSOK", e.getMessage()));
        }
    }

    // 저장한 공유 약속 목록
    @GetMapping
    public ResponseEntity<ApiResponse<List<YaksokDto>>> getSharedYaksokList(@RequestParam String userEmail) {
        try {
            List<YaksokDto> list = shareYaksokService.getSharedYaksokList(userEmail);
            return ResponseEntity.ok(ApiResponse.success("공유 약속 목록을 가져왔습니다.", list));
        } catch (Exception e) {
            log.error("공유 약속 목록 조회 실패 : {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "FAIL_GET_SHARED_YAKSOK_LIST", e.getMessage()));
        }
    }

    // 내 목록에서 빼기. 저장한 본인만 가능하다.
    @DeleteMapping("/{yaksokId}")
    public ResponseEntity<ApiResponse<Void>> deleteSharedYaksok(
            @PathVariable Long yaksokId,
            @RequestParam String userEmail) {
        try {
            shareYaksokService.deleteSharedYaksok(userEmail, yaksokId);
            return ResponseEntity.ok(ApiResponse.success("공유 목록에서 제거하였습니다."));
        } catch (Exception e) {
            log.error("공유 약속 삭제 실패 : {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "FAIL_DELETE_SHARED_YAKSOK", e.getMessage()));
        }
    }
}
