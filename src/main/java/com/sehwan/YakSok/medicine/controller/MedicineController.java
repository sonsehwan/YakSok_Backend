package com.sehwan.YakSok.medicine.controller;

import com.sehwan.YakSok.common.response.ApiResponse;
import com.sehwan.YakSok.medicine.entity.SimpleMedicine;
import com.sehwan.YakSok.medicine.service.MedicineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/medicine")
public class MedicineController {

    private final MedicineService medicineService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<SimpleMedicine>>> searchMedicine(@RequestParam String keyword, @RequestParam int pageNo) {
        try{
            List<SimpleMedicine> medicineList;

            if(keyword != null || !keyword.isEmpty()){
                medicineList = medicineService.searchMedicineList(keyword, pageNo, 100);
                System.out.println("통신 성공");
            }else{
                medicineList = medicineService.fetchAllMedicineList(pageNo, 100);
                System.out.println("통신 성공");
            }

            if(medicineList.isEmpty()){
                return ResponseEntity.ok(ApiResponse.success("검색 결과가 없습니다.", medicineList));
            }

            return ResponseEntity.ok(ApiResponse.success("약 검색 결과입니다.", medicineList));
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "약 검색 실패", e.getMessage()));
        }
    }
}
