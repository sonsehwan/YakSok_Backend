package com.sehwan.YakSok.yaksok.controller;


import com.sehwan.YakSok.common.response.ApiResponse;
import com.sehwan.YakSok.yaksok.dto.YaksokRequest;
import com.sehwan.YakSok.yaksok.entity.Yaksok;
import com.sehwan.YakSok.yaksok.service.YaksokService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/yaksok")
@RequiredArgsConstructor
public class YaksokController {
    private final YaksokService yaksokService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> saveYaksok(@RequestBody YaksokRequest request) {
        try{
            yaksokService.saveYaksok(request);
            return ResponseEntity.ok(ApiResponse.success("약속 저장에 성공하였습니다."));
        }catch(Exception e){
            log.error("에러 발생 : {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(400,"FAIL_SAVE_YAKSOK", e.getMessage()));
        }
    }
}
