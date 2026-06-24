package com.sehwan.YakSok.drugstore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sehwan.YakSok.drugstore.dto.SearchDrugStoreDto;
import com.sehwan.YakSok.drugstore.entity.DrugStore;
import com.sehwan.YakSok.drugstore.repository.DrugStoreRepository;
import com.sehwan.YakSok.drugstore.dto.DrugStoreDto;
import com.sehwan.YakSok.user.entity.User;
import com.sehwan.YakSok.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DrugstoreService {

    private final DrugStoreRepository  drugstoreRepository;
    private final UserRepository userRepository;

    @Qualifier("drugstoreRestClient")
    private final RestClient drugstoreRestClient;

    @Value("${api.medicine.service-key}")
    private String serviceKey;

    @Transactional
    public List<DrugStoreDto> getCloseDrugStoreList(String latitude, String longitude, int page) {
        return callApi(latitude, longitude, page, 20);
    }

    @Transactional
    public List<SearchDrugStoreDto> getSearchDrugStoreList(String firstAddress, String secondAddress, String name) {
        return callSearchApi(firstAddress, secondAddress, name);
    }

//    private List<SearchDrugStoreDto> callSearchApi(String firstAddress, String secondAddress, String name) {
//        try{
//
//            log.info("검색 요청 파라미터 firstAddress={}, secondAddress={}, name={}",
//                    firstAddress, secondAddress, name);
//
//            String responseJson = drugstoreRestClient.get()
//                    .uri(uriBuilder -> uriBuilder
//                            .path("/getParmacyListInfoInqire")
//                            .queryParam("ServiceKey", serviceKey)
//                            .queryParam("Q0", firstAddress)
//                            .queryParam("Q1", secondAddress)
//                            .queryParam("QN", name)
//                            .queryParam("_type", "json")
//                            .build())
//                    .retrieve()
//                    .body(String.class);
//
//            log.info("===== [공공데이터 API 실제 응답] =====");
//            log.info(responseJson);
//            log.info("====================================");
//
//            ObjectMapper mapper = new ObjectMapper();
//            JsonNode rootNode = mapper.readTree(responseJson);
//
//            log.info("rootNode = {}", rootNode.toPrettyString());
//            log.info("body = {}", rootNode.path("response").path("body").toPrettyString());
//            log.info("items = {}", rootNode.path("response").path("body").path("items").toPrettyString());
//            log.info("item = {}", rootNode.path("response").path("body").path("items").path("item").toPrettyString());
//
//            JsonNode itemNode = rootNode.path("response").path("body").path("items").path("item");
//
//            if (itemNode.isMissingNode() || itemNode.isNull() || itemNode.isEmpty()) {
//                log.error("해당 위치 주변에 검색된 약국이 없습니다.");
//                return new ArrayList<>();
//            }
//
//            if (itemNode.isObject()) {
//                SearchDrugStoreDto singleStore = mapper.treeToValue(itemNode, SearchDrugStoreDto.class);
//                List<SearchDrugStoreDto> list = new ArrayList<>();
//                list.add(singleStore);
//                System.out.println(list);
//
//                return list;
//            } else if (itemNode.isArray()) {
//                System.out.println(mapper.convertValue(itemNode, new TypeReference<List<SearchDrugStoreDto>>() {}));
//                List<SearchDrugStoreDto> list = mapper.convertValue(itemNode, new TypeReference<List<SearchDrugStoreDto>>() {});
//
//                return list;
//            }
//
//        } catch (RestClientResponseException e) {
//            log.error("공공 API HTTP 상태코드 = {}", e.getStatusCode());
//            log.error("공공 API 에러 응답 바디 = {}", e.getResponseBodyAsString(StandardCharsets.UTF_8));
//            e.printStackTrace();
//        } catch (JsonProcessingException e) {
//            log.error("JSON 파싱 에러 = {}", e.getMessage());
//            e.printStackTrace();
//        } catch (Exception e) {
//            log.error("약국 API 통신 중 에러 발생 = {}", e.getMessage());
//            e.printStackTrace();
//        }
//        return new ArrayList<>();
//    }

    private List<SearchDrugStoreDto> callSearchApi(String firstAddress, String secondAddress, String name) {
        try {
            log.info("===== 약국 검색 요청 파라미터 =====");
            log.info("firstAddress = {}", firstAddress);
            log.info("secondAddress = {}", secondAddress);
            log.info("name = {}", name);

            PublicApiResult apiResult = drugstoreRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/getParmacyListInfoInqire")
                            .queryParam("ServiceKey", serviceKey)
                            .queryParam("Q0", firstAddress)
                            .queryParam("Q1", secondAddress)
                            .queryParam("QN", name)
                            .queryParam("pageNo", 1)
                            .queryParam("numOfRows", 20)
                            .queryParam("_type", "json")
                            .build())
                    .exchange((request, response) -> {
                        String body = StreamUtils.copyToString(
                                response.getBody(),
                                StandardCharsets.UTF_8
                        );

                        String requestUrl = request.getURI().toString();
                        HttpStatusCode statusCode = response.getStatusCode();

                        log.error("===== 공공 API 요청 URL =====");
                        log.error("{}", requestUrl);

                        log.error("===== 공공 API 상태 코드 =====");
                        log.error("{}", statusCode);

                        log.error("===== 공공 API 응답 바디 =====");
                        log.error("{}", body);

                        System.out.println("===== 공공 API 요청 URL =====");
                        System.out.println(requestUrl);

                        System.out.println("===== 공공 API 상태 코드 =====");
                        System.out.println(statusCode);

                        System.out.println("===== 공공 API 응답 바디 =====");
                        System.out.println(body);

                        return new PublicApiResult(requestUrl, statusCode, body);
                    });

            if (apiResult == null) {
                log.error("공공 API 응답 결과 객체가 null입니다.");
                return new ArrayList<>();
            }

            if (!apiResult.statusCode().is2xxSuccessful()) {
                log.error("공공 API 호출 실패. statusCode={}, body={}",
                        apiResult.statusCode(),
                        apiResult.body());
                return new ArrayList<>();
            }

            String responseJson = apiResult.body();

            if (responseJson == null || responseJson.isBlank()) {
                log.error("공공 API 응답 body가 비어있습니다.");
                return new ArrayList<>();
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(responseJson);

            log.info("===== 공공 API JSON 파싱 결과 =====");
            log.info("rootNode = {}", rootNode.toPrettyString());
            log.info("header = {}", rootNode.path("response").path("header").toPrettyString());
            log.info("body = {}", rootNode.path("response").path("body").toPrettyString());
            log.info("items = {}", rootNode.path("response").path("body").path("items").toPrettyString());

            JsonNode bodyNode = rootNode.path("response").path("body");
            JsonNode itemsNode = bodyNode.path("items");

            JsonNode itemNode;

            if (itemsNode.has("item")) {
                itemNode = itemsNode.path("item");
            } else {
                itemNode = itemsNode;
            }

            log.info("itemNode = {}", itemNode.toPrettyString());

            if (itemNode.isMissingNode() || itemNode.isNull() || itemNode.isEmpty()) {
                log.error("검색된 약국이 없습니다.");
                log.error("공공 API body = {}", bodyNode.toPrettyString());
                return new ArrayList<>();
            }

            if (itemNode.isObject()) {
                SearchDrugStoreDto singleStore =
                        mapper.treeToValue(itemNode, SearchDrugStoreDto.class);

                List<SearchDrugStoreDto> list = new ArrayList<>();
                list.add(singleStore);

                log.info("검색 결과 개수 = {}", list.size());
                return list;
            }

            if (itemNode.isArray()) {
                List<SearchDrugStoreDto> list =
                        mapper.convertValue(itemNode, new TypeReference<List<SearchDrugStoreDto>>() {});

                log.info("검색 결과 개수 = {}", list.size());
                return list;
            }

            log.error("예상하지 못한 itemNode 구조입니다. itemNode={}", itemNode.toPrettyString());
            return new ArrayList<>();

        } catch (JsonProcessingException e) {
            log.error("응답 데이터를 SearchDrugStoreDto로 변환하는 중 에러 발생: {}", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            log.error("약국 검색 API 통신 중 에러 발생: {}", e.getMessage());
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    private record PublicApiResult(
            String requestUrl,
            HttpStatusCode statusCode,
            String body
    ) {
    }

    private List<DrugStoreDto> callApi(String latitude, String longitude, int pageNo, int numOfRows) {
        try {
            String responseJson = drugstoreRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/getParmacyLcinfoInqire")
                            .queryParam("ServiceKey", serviceKey)
                            .queryParam("WGS84_LAT", latitude)
                            .queryParam("WGS84_LON", longitude)
                            .queryParam("pageNo", pageNo)
                            .queryParam("numOfRows", numOfRows)
                            .queryParam("_type", "json")
                            .build())
                    .retrieve()
                    .body(String.class);

            log.info("===== [공공데이터 API 실제 응답] =====");
            log.info(responseJson);
            log.info("====================================");

            if (responseJson == null || responseJson.isEmpty()) {
                log.error("공공데이터 API 응답이 null이거나 비어있습니다.");
                return new ArrayList<>();
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(responseJson);

            JsonNode itemNode = rootNode.path("response").path("body").path("items").path("item");

            if (itemNode.isMissingNode() || itemNode.isNull() || itemNode.isEmpty()) {
                log.error("해당 위치 주변에 검색된 약국이 없습니다.");
                return new ArrayList<>();
            }

            if (itemNode.isObject()) {
                DrugStoreDto singleStore = mapper.treeToValue(itemNode, DrugStoreDto.class);
                List<DrugStoreDto> list = new ArrayList<>();
                list.add(singleStore);
                System.out.println(list);

                //임시로 첫페이지 첫번째 약국의 Entity생성하여 약사 유저와 연결
//                if(pageNo == 1){
//                    if(!drugstoreRepository.existsByHpid(singleStore.getHpid())){
//                        DrugStore changeDrugStore = singleStore.toEntity();
//                        DrugStore drugStore = drugstoreRepository.save(changeDrugStore);
//
//                        User user = userRepository.findByEmail("test1@naver.com")
//                                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
//
//                        user.setMyDrugStore(drugStore);
//                        userRepository.save(user);
//                    }else{
//                        DrugStore drugStore = drugstoreRepository.findByHpid(singleStore.getHpid());
//                        User user = userRepository.findByEmail("test1@naver.com")
//                                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
//
//                        user.setMyDrugStore(drugStore);
//                        userRepository.save(user);
//                    }
//                }
                return list;
            } else if (itemNode.isArray()) {
                System.out.println(mapper.convertValue(itemNode, new TypeReference<List<DrugStoreDto>>() {}));
                List<DrugStoreDto> list = mapper.convertValue(itemNode, new TypeReference<List<DrugStoreDto>>() {});

//                if(pageNo == 1){
//                    if(!drugstoreRepository.existsByHpid(list.getFirst().getHpid())){
//                        DrugStore changeDrugStore = list.getFirst().toEntity();
//                        DrugStore drugStore = drugstoreRepository.save(changeDrugStore);
//
//                        User user = userRepository.findByEmail("test1@naver.com")
//                                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
//
//                        user.setMyDrugStore(drugStore);
//                        userRepository.save(user);
//                    }else{
//                        DrugStore drugStore = drugstoreRepository.findByHpid(list.getFirst().getHpid());
//                        User user = userRepository.findByEmail("test1@naver.com")
//                                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
//
//                        user.setMyDrugStore(drugStore);
//                        userRepository.save(user);
//                    }
//                }
                return list;
            }

        } catch (JsonProcessingException e) {
            log.error("응답 데이터를 DrugStore 객체로 변환하는 중 에러 발생: {}", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            log.error("약국 API 통신 중 에러 발생: {}", e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
