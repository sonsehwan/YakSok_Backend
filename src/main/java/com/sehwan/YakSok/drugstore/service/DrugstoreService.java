package com.sehwan.YakSok.drugstore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sehwan.YakSok.drugstore.dto.DrugStore;
import com.sehwan.YakSok.medicine.entity.SimpleMedicine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class DrugstoreService {

    @Qualifier("drugstoreRestClient")
    private final RestClient drugstoreRestClient;

    @Value("${api.medicine.service-key}")
    private String serviceKey;


    public List<DrugStore> getCloseDrugStoreList(String latitude, String longitude) {
        return callApi(latitude, longitude, 1, 20);
    }

    private List<DrugStore> callApi(String latitude, String longitude, int pageNo, int numOfRows) {
        try{
            JsonNode rootNode = drugstoreRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("ServiceKey", serviceKey)
                            .queryParam("WGS84_LAT", latitude)
                            .queryParam("WGS84_LON", longitude)
                            .queryParam("pageNo", pageNo)
                            .queryParam("numOfRows", numOfRows)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);

            if(rootNode == null){
                log.error("공공데이터 API 응답이 null입니다.");
                return new ArrayList<>();
            }

            JsonNode itemNode = rootNode.path("response").path("body").path("items").path("item");

            if(itemNode.isMissingNode() || itemNode.isNull() || itemNode.isEmpty()){
                log.error("해당 위치 주변에 검색된 약국이 없습니다.");
                return new ArrayList<>();
            }

            ObjectMapper mapper = new ObjectMapper();

            if (itemNode.isObject()) {
                DrugStore singleStore = mapper.treeToValue(itemNode, DrugStore.class);
                List<DrugStore> list = new ArrayList<>();
                list.add(singleStore);
                return list;
            }

            else if (itemNode.isArray()) {
                return mapper.convertValue(itemNode, new TypeReference<List<DrugStore>>() {});
            }

        }catch (JsonProcessingException e){
            log.error("응답 데이터를 DrugStore 객체로 변환하는 중 에러 발생: ", e.getMessage());
            e.printStackTrace();
        }catch (Exception e){
            log.error("약국 API 통신 중 에러 발생: " + e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
