package com.sehwan.YakSok.drugstore.controller;

import com.sehwan.YakSok.common.response.ApiResponse;
import com.sehwan.YakSok.drugstore.dto.DrugStore;
import com.sehwan.YakSok.drugstore.service.DrugstoreService;
import com.sehwan.YakSok.user.service.UserService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import static com.sehwan.YakSok.common.response.ApiResponse.success;

@Controller
@RequiredArgsConstructor
@RequestMapping("api/drugstore")
public class DrugstoreController {

    private final DrugstoreService drugstoreService;
    private final UserService userService;

    @GetMapping("/closelist")
    public ResponseEntity<ApiResponse<List<DrugStore>>> getCloseDrugstores(
            //@RequestParam String email,
            @RequestParam String latitude,
            @RequestParam String longitude,
            @RequestParam int page
    ){
        List<DrugStore> drugStores;

        drugStores = drugstoreService.getCloseDrugStoreList(latitude, longitude, page);

        return ResponseEntity.ok(success("근접 약국 리스트를 성공적으로 가져왔습니다.", drugStores));
    }
}
