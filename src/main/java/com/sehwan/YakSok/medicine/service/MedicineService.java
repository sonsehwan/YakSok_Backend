package com.sehwan.YakSok.medicine.service;

import com.sehwan.YakSok.medicine.entity.SimpleMedicine;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicineService {

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
                            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
                            uriBuilder.queryParam("item_name", encodedKeyword);
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
            Map<String, Object> rootResponse = (Map<String, Object>) response.get("getMdcinGrnIdntfcInfoList03_response");

            if (rootResponse == null) {
                rootResponse = (Map<String, Object>) response.get("response");
            }

            if (rootResponse != null && rootResponse.containsKey("body")) {
                body = (Map<String, Object>) rootResponse.get("body");
            }

            if (body == null) {
                System.out.println("응답에서 'body' 객체를 찾을 수 없습니다.");
                return new ArrayList<>();
            }

            List<Map<String, Object>> items = new ArrayList<>();
            Object itemsObj = body.get("items");

            if (itemsObj instanceof Map) {
                Map<String, Object> itemsMap = (Map<String, Object>) itemsObj;
                Object itemEntry = itemsMap.get("item");
                if (itemEntry instanceof List) {
                    items = (List<Map<String, Object>>) itemEntry;
                } else if (itemEntry instanceof Map) {
                    items.add((Map<String, Object>) itemEntry);
                }
            } else if (itemsObj instanceof List) {
                items = (List<Map<String, Object>>) itemsObj;
            }

            if (items.isEmpty()) {
                System.out.println("검색 결과가 없습니다.");
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