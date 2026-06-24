package com.sehwan.YakSok.drugstore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sehwan.YakSok.drugstore.dto.SearchDrugStoreDto;
import com.sehwan.YakSok.drugstore.entity.DrugStore;
import com.sehwan.YakSok.drugstore.repository.DrugStoreRepository;
import com.sehwan.YakSok.drugstore.dto.DrugStoreDto;
import com.sehwan.YakSok.user.dto.UserResponse;
import com.sehwan.YakSok.user.entity.User;
import com.sehwan.YakSok.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;

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

    private List<SearchDrugStoreDto> callSearchApi(String firstAddress, String secondAddress, String name) {
        try {
            log.info("===== 약국 검색 요청 파라미터 =====");
            log.info("firstAddress = {}", firstAddress);
            log.info("secondAddress = {}", secondAddress);
            log.info("name = {}", name);

            URI uri = UriComponentsBuilder
                    .fromUriString("https://apis.data.go.kr/B552657/ErmctInsttInfoInqireService/getParmacyListInfoInqire")
                    .queryParam("ServiceKey", serviceKey)
                    .queryParam("Q0", firstAddress)
                    .queryParam("Q1", secondAddress)
                    .queryParam("QN", name)
                    .queryParam("pageNo", 1)
                    .queryParam("numOfRows", 20)
                    .queryParam("_type", "json")
                    .encode()
                    .build()
                    .toUri();

            log.error("===== 최종 공공 API 요청 URI =====");
            log.error("{}", uri);
            log.error("ASCII URI = {}", uri.toASCIIString());

            String responseJson = RestClient.create()
                    .get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);

            log.info("===== 공공 API 응답 =====");
            log.info(responseJson);

            if (responseJson == null || responseJson.isBlank()) {
                log.error("공공 API 응답이 비어있습니다.");
                return new ArrayList<>();
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(responseJson);

            JsonNode itemNode = rootNode
                    .path("response")
                    .path("body")
                    .path("items")
                    .path("item");

            if (itemNode.isMissingNode() || itemNode.isNull() || itemNode.isEmpty()) {
                log.error("검색된 약국이 없습니다.");
                log.error("body = {}", rootNode.path("response").path("body").toPrettyString());
                return new ArrayList<>();
            }

            if (itemNode.isObject()) {
                SearchDrugStoreDto singleStore =
                        mapper.treeToValue(itemNode, SearchDrugStoreDto.class);

                List<SearchDrugStoreDto> list = new ArrayList<>();
                list.add(singleStore);
                return list;
            }

            if (itemNode.isArray()) {
                return mapper.convertValue(
                        itemNode,
                        new TypeReference<List<SearchDrugStoreDto>>() {}
                );
            }

            log.error("예상하지 못한 itemNode 구조 = {}", itemNode.toPrettyString());

        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 에러: {}", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            log.error("약국 검색 API 통신 에러: {}", e.getMessage());
            e.printStackTrace();
        }

        return new ArrayList<>();
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

    @Transactional
    public UserResponse createDrugStore(String email, SearchDrugStoreDto drugStore) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        if (drugStore == null || drugStore.getHpid() == null || drugStore.getHpid().isBlank()) {

            if(drugStore == null){
                log.error("객체가 있는가: false");
            }else{
                log.error("객체가 있는가: true");
            }
            if(drugStore.getHpid() == null){
                log.error("객체에 id가 있는가?: false");
            }else{
                log.error("객체에 id가 있는가?: true");
            }
            if(drugStore.getHpid().isBlank()){
                log.error("id가 비어있는가?: false");
            }else{
                log.error("id가 비어있는가?: true");
            }

            throw new RuntimeException("약국 정보가 올바르지 않습니다.");
        }

        String hpid = drugStore.getHpid();

        if (userRepository.existsByMyDrugStore_HpidAndEmailNot(hpid, email)) {
            throw new RuntimeException("이미 다른 사용자가 등록한 약국입니다.");
        }

        DrugStore drugStoreEntity = drugstoreRepository.findByHpid(hpid)
                .orElseGet(() -> drugstoreRepository.save(drugStore.toEntity()));

        user.setMyDrugStore(drugStoreEntity);

        return new UserResponse(user);
    }
}
