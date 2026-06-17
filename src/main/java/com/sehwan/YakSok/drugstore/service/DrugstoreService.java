package com.sehwan.YakSok.drugstore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sehwan.YakSok.drugstore.entity.DrugStore;
import com.sehwan.YakSok.drugstore.repository.DrugStoreRepository;
import com.sehwan.YakSok.drugstore.dto.DrugStoreDto;
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
                if(pageNo == 1){
                    if(!drugstoreRepository.existsByHpid(singleStore.getHpid())){
                        DrugStore changeDrugStore = singleStore.toEntity();
                        DrugStore drugStore = drugstoreRepository.save(changeDrugStore);

                        User user = userRepository.findByEmail("test2@naver.com")
                                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

                        user.setMyDrugStore(drugStore);
                        userRepository.save(user);
                    }else{
                        DrugStore drugStore = drugstoreRepository.findByHpid(singleStore.getHpid());
                        User user = userRepository.findByEmail("test2@naver.com")
                                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

                        user.setMyDrugStore(drugStore);
                        userRepository.save(user);
                    }
                }
                return list;
            } else if (itemNode.isArray()) {
                System.out.println(mapper.convertValue(itemNode, new TypeReference<List<DrugStoreDto>>() {}));
                List<DrugStoreDto> list = mapper.convertValue(itemNode, new TypeReference<List<DrugStoreDto>>() {});

                if(pageNo == 1){
                    if(!drugstoreRepository.existsByHpid(list.getFirst().getHpid())){
                        DrugStore changeDrugStore = list.getFirst().toEntity();
                        DrugStore drugStore = drugstoreRepository.save(changeDrugStore);

                        User user = userRepository.findByEmail("test2@naver.com")
                                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

                        user.setMyDrugStore(drugStore);
                        userRepository.save(user);
                    }else{
                        DrugStore drugStore = drugstoreRepository.findByHpid(list.getFirst().getHpid());
                        User user = userRepository.findByEmail("test2@naver.com")
                                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

                        user.setMyDrugStore(drugStore);
                        userRepository.save(user);
                    }
                }
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
