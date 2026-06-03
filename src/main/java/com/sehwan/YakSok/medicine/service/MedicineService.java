package com.sehwan.YakSok.medicine.service;

import com.sehwan.YakSok.medicine.entity.SimpleMedicine;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicineService {

    // WebClient 대신 RestClient 주입
    @Qualifier("medicineRestClient")
    private final RestClient medicineRestClient;

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

    public SimpleMedicine fetchPill(String keyword) {
        return callApi(keyword);
    }

    private List<SimpleMedicine> callApi(String keyword, int pageNo, int numOfRows) {
        try {
            Map response = medicineRestClient.get()
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
                    .body(Map.class);

            if (response == null) {
                System.out.println("공공데이터 API 응답이 null입니다.");
                return new ArrayList<>();
            }

            Map<String, Object> body = null;

            if (response.containsKey("body")) {
                body = (Map<String, Object>) response.get("body");
            }
            else {
                Map<String, Object> rootResponse = (Map<String, Object>) response.get("getMdcinGrnIdntfcInfoList03_response");
                if (rootResponse == null) {
                    rootResponse = (Map<String, Object>) response.get("response");
                }

                if (rootResponse != null && rootResponse.containsKey("body")) {
                    body = (Map<String, Object>) rootResponse.get("body");
                }
            }

            if (body == null) {
                System.out.println("응답에서 'body' 객체를 찾을 수 없습니다. 응답 구조: " + response);
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

    private SimpleMedicine callApi(String keyword) {
        try {
            Map response = medicineRestClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder
                                .queryParam("serviceKey", serviceKey)
                                .queryParam("type", "json")
                                .queryParam("pageNo", 1)
                                .queryParam("numOfRows", 1);

                        if (keyword != null && !keyword.trim().isEmpty()) {
                            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
                            uriBuilder.queryParam("item_name", encodedKeyword);
                        }

                        return uriBuilder.build();
                    })
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                System.out.println("공공데이터 API 통신 응답 자체가 null입니다.");
                return null;
            }

            // --- 아래 파싱 로직 기존과 동일 ---
            Map<String, Object> body = null;

            if (response.containsKey("body")) {
                body = (Map<String, Object>) response.get("body");
            }
            else {
                Map<String, Object> rootResponse = (Map<String, Object>) response.get("getMdcinGrnIdntfcInfoList03_response");
                if (rootResponse == null) {
                    rootResponse = (Map<String, Object>) response.get("response");
                }

                if (rootResponse != null && rootResponse.containsKey("body")) {
                    body = (Map<String, Object>) rootResponse.get("body");
                }
            }

            if (body == null) {
                System.out.println("응답에서 'body' 객체를 찾을 수 없습니다. 응답 구조: " + response);
                return null;
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
                System.out.println("'" + keyword + "' 검색 결과가 없습니다. 이름 정제 후 재검색을 시도합니다.");
                return reCallApi(keyword);
            }

            Map<String, Object> firstItem = items.get(0);
            return new SimpleMedicine(
                    (String) firstItem.get("ITEM_NAME"),
                    (String) firstItem.get("ITEM_IMAGE")
            );

        } catch (Exception e) {
            System.err.println("공공데이터 API 통신 중 에러 발생: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private SimpleMedicine reCallApi(String keyword) {
        int index = keyword.indexOf("정");

        if (index == -1) {
            System.out.println("약 이름에 '정'이 포함되어 있지 않아 재검색을 종료합니다.");
            return null;
        }

        String itemName = keyword.substring(0, index + 1);
        System.out.println("정제된 약 이름으로 재검색: " + itemName);

        try {
            Map response = medicineRestClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder
                                .queryParam("serviceKey", serviceKey)
                                .queryParam("type", "json")
                                .queryParam("pageNo", 1)
                                .queryParam("numOfRows", 1);

                        if (itemName != null && !itemName.trim().isEmpty()) {
                            String encodedKeyword = URLEncoder.encode(itemName, StandardCharsets.UTF_8);
                            uriBuilder.queryParam("item_name", encodedKeyword);
                        }

                        return uriBuilder.build();
                    })
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                return null;
            }

            Map<String, Object> body = null;

            if (response.containsKey("body")) {
                body = (Map<String, Object>) response.get("body");
            }
            else {
                Map<String, Object> rootResponse = (Map<String, Object>) response.get("getMdcinGrnIdntfcInfoList03_response");
                if (rootResponse == null) {
                    rootResponse = (Map<String, Object>) response.get("response");
                }

                if (rootResponse != null && rootResponse.containsKey("body")) {
                    body = (Map<String, Object>) rootResponse.get("body");
                }
            }

            if (body == null) {
                return null;
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
                System.out.println("재검색 결과도 없습니다.");
                return null;
            }

            Map<String, Object> firstItem = items.get(0);
            return new SimpleMedicine(
                    (String) firstItem.get("ITEM_NAME"),
                    (String) firstItem.get("ITEM_IMAGE")
            );

        } catch (Exception e) {
            System.err.println("재검색 통신 중 에러 발생: " + e.getMessage());
            return null;
        }
    }
}