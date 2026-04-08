package com.sehwan.YakSok.medicine.service;

import com.sehwan.YakSok.medicine.entity.SimpleMedicine;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicineService {

    // 빈이 여러개일 때 어떤 빈을 주입할 것이지 명시
    @Qualifier("medicineWebClient")
    private final WebClient medicineWebClient;

    @Value("${api.medicine.service-key}")
    private String serviceKey;

    @Value("${api.medicine.url}")
    private String openApiUrl;

    public List<SimpleMedicine> fetchAllMedicineList(int pageNo, int numOfRows) {
        return callApi(null, pageNo, numOfRows);
    }

    public List<SimpleMedicine> searchMedicineList(String keyword, int pageNo, int numOfRows) {
        return callApi(keyword, pageNo, numOfRows);
    }

    private List<SimpleMedicine> callApi(String keyword, int pageNo, int numOfRows) {

        try {
            Map<String, Object> response = medicineWebClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder
                                .queryParam("serviceKey", serviceKey)
                                .queryParam("type", "json")
                                .queryParam("pageNo", pageNo)
                                .queryParam("numOfRows", numOfRows);

                        if (keyword != null && !keyword.trim().isEmpty()) {
                            uriBuilder.queryParam("item_name", keyword);
                        }

                        return uriBuilder.build();
                    })
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();


            if (response == null) {
                System.out.println("공공데이터 API 응답이 null입니다.");
                return new ArrayList<>();
            }

            Map<String, Object> body = null;

            // 2-1. 바로 body가 있는 경우 (일부 API)
            if (response.containsKey("body")) {
                body = (Map<String, Object>) response.get("body");
            }
            // 2-2. "response" 안에 "body"가 있는 경우 (대부분의 공공데이터 API)
            else if (response.containsKey("response")) {
                Map<String, Object> outerResponse = (Map<String, Object>) response.get("response");
                if(outerResponse != null && outerResponse.containsKey("body")){
                    body = (Map<String, Object>) outerResponse.get("body");
                }
            }

            if (body == null) {
                System.out.println("응답에서 'body' 객체를 찾을 수 없습니다. 응답 형태: " + response.toString());
                return new ArrayList<>();
            }

            List<Map<String, Object>> items = null;

            if(body.get("items") instanceof List){
                items = (List<Map<String, Object>>) body.get("items");
            }

            if (items == null || items.isEmpty()) {
                System.out.println("body 안에서 'items' 리스트를 찾을 수 없거나 데이터가 없습니다.");
                return new ArrayList<>();
            }

            return items.stream()
                    .map(item -> new SimpleMedicine(
                            (String) item.get("ITEM_NAME"),
                            (String) item.get("ITEM_IMAGE")
                    ))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("공공데이터 API 통신 중 에러 발생: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}