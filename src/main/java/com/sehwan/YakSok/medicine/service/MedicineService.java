package com.sehwan.YakSok.medicine.service;

import com.sehwan.YakSok.medicine.entity.SimpleMedicine;
import lombok.RequiredArgsConstructor;
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

    private final WebClient webClient;

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
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(openApiUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

        try {
            Map<String, Object> response = webClient.get()
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

            if (response == null || !response.containsKey("body")) return new ArrayList<>();

            Map<String, Object> body = (Map<String, Object>) response.get("body");
            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");

            if (items == null) return new ArrayList<>();

            return items.stream()
                    .map(item -> new SimpleMedicine(
                            (String) item.get("ITEM_NAME"),
                            (String) item.get("ITEM_IMAGE")
                    ))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}