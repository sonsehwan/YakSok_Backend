package com.sehwan.YakSok.yaksok.controller;

import com.sehwan.YakSok.common.response.ApiResponse;
import com.sehwan.YakSok.user.dto.response.SharedUser;
import com.sehwan.YakSok.yaksok.dto.ShareYaksokRequest;
import com.sehwan.YakSok.yaksok.dto.YaksokDto;
import com.sehwan.YakSok.yaksok.service.ShareYaksokService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<ApiResponse<Void>> saveSharedYaksok(@RequestBody ShareYaksokRequest request,
                                                               @AuthenticationPrincipal Long userId) {
        try {
            shareYaksokService.saveSharedYaksok(userId, request.getYaksokId());
            return ResponseEntity.ok(ApiResponse.success("공유 약속을 저장하였습니다."));
        } catch (Exception e) {
            log.error("공유 약속 저장 실패 : {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "FAIL_SAVE_SHARED_YAKSOK", e.getMessage()));
        }
    }

    // 해당 id 유저에게 공유한 유저 목록
    @GetMapping("/sender")
    public ResponseEntity<ApiResponse<List<SharedUser>>> getSharedUserList(@AuthenticationPrincipal Long userId) {
        try {
            List<SharedUser> list = shareYaksokService.getSharedUserList(userId);
            return ResponseEntity.ok(ApiResponse.success("공유자 목록을 성공적으로 가져왔습니다.", list));
        }catch (Exception e){
            log.error("공유자 목록 조회 실패 : {}",e .getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "FAIL_GET_SHARED_USERLIST", e.getMessage()));
        }
    }

    // 공유받은 약속 상세 (채팅 "확인하기" 미리보기 + 이미 저장한 목록 상세 겸용)
    @GetMapping("/{yaksokId}")
    public ResponseEntity<ApiResponse<YaksokDto>> getSharedYaksokDetail(
            @PathVariable Long yaksokId,
            @AuthenticationPrincipal Long userId) {
        try {
            YaksokDto yaksok = shareYaksokService.getSharedYaksokDetail(userId, yaksokId);
            return ResponseEntity.ok(ApiResponse.success("공유 약속을 성공적으로 가져왔습니다.", yaksok));
        } catch (Exception e) {
            log.error("공유 약속 상세 조회 실패 : {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "FAIL_GET_SHARED_YAKSOK_DETAIL", e.getMessage()));
        }
    }

    // 저장한 공유 약속 목록
    @GetMapping
    public ResponseEntity<ApiResponse<List<YaksokDto>>> getSharedYaksokList(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Long senderId) {
        try {
            List<YaksokDto> list = shareYaksokService.getSharedYaksokList(userId, senderId);
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
            @AuthenticationPrincipal Long userId) {
        try {
            shareYaksokService.deleteSharedYaksok(userId, yaksokId);
            return ResponseEntity.ok(ApiResponse.success("공유 목록에서 제거하였습니다."));
        } catch (Exception e) {
            log.error("공유 약속 삭제 실패 : {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "FAIL_DELETE_SHARED_YAKSOK", e.getMessage()));
        }
    }
}
