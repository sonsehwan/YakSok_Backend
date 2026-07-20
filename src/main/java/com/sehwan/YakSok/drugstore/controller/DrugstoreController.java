package com.sehwan.YakSok.drugstore.controller;

import com.sehwan.YakSok.common.response.ApiResponse;
import com.sehwan.YakSok.drugstore.dto.CreateDrugStoreRequest;
import com.sehwan.YakSok.drugstore.dto.DrugStoreDto;
import com.sehwan.YakSok.drugstore.dto.SearchDrugStoreDto;
import com.sehwan.YakSok.drugstore.service.DrugstoreService;
import com.sehwan.YakSok.user.dto.response.UserResponse;
import com.sehwan.YakSok.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.sehwan.YakSok.common.response.ApiResponse.success;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/drugstore")
public class DrugstoreController {

    private final DrugstoreService drugstoreService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createDrugStore(@RequestBody CreateDrugStoreRequest request){
        String email = request.getEmail();
        SearchDrugStoreDto drugStore = request.getDrugStore();

        UserResponse response = drugstoreService.createDrugStore(email, drugStore);

        return ResponseEntity.ok(success("약국을 성공적으로 생성 및 유저와 연결하였습니다.", response));
    }

    @GetMapping("/closelist")
    public ResponseEntity<ApiResponse<List<DrugStoreDto>>> getCloseDrugstores(
            //@RequestParam String email,
            @RequestParam String latitude,
            @RequestParam String longitude,
            @RequestParam int page
    ){
        List<DrugStoreDto> drugStores;

        drugStores = drugstoreService.getCloseDrugStoreList(latitude, longitude, page);

        return ResponseEntity.ok(success("근접 약국 리스트를 성공적으로 가져왔습니다.", drugStores));
    }

    @GetMapping("/searchlist")
    public ResponseEntity<ApiResponse<List<SearchDrugStoreDto>>> getSearchDrugstores(
            @RequestParam String firstAddress,
            @RequestParam String secondAddress,
            @RequestParam String name
    ){
        List<SearchDrugStoreDto> drugStores;

        drugStores = drugstoreService.getSearchDrugStoreList(firstAddress, secondAddress, name);

        return ResponseEntity.ok(success("근접 약국 리스트를 성공적으로 가져왔습니다.", drugStores));
    }
}
