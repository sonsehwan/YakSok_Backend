package com.sehwan.YakSok.user.service;


import com.sehwan.YakSok.drugstore.dto.SearchDrugStoreDto;
import com.sehwan.YakSok.drugstore.entity.DrugStore;
import com.sehwan.YakSok.drugstore.repository.DrugStoreRepository;
import com.sehwan.YakSok.user.dto.request.LoginRequest;
import com.sehwan.YakSok.user.dto.request.ModifyInfoRequest;
import com.sehwan.YakSok.user.dto.request.ModifyPasswordRequest;
import com.sehwan.YakSok.user.dto.request.UserRequest;
import com.sehwan.YakSok.user.dto.response.UserResponse;
import com.sehwan.YakSok.user.entity.User;
import com.sehwan.YakSok.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final DrugStoreRepository drugStoreRepository;

    @Transactional
    public void signUp(UserRequest dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }

        if(userRepository.existsByNickname(dto.getNickname())) {
            throw  new RuntimeException("이미 존재하는 닉네임입니다.");
        }

        if(!dto.getRole().equals("DRUGSTORE")) {
            User user = dto.toEntity();
            userRepository.save(user);
        }else{
            SearchDrugStoreDto drugStore = dto.getDrugStore();
            String hpid = drugStore.getHpid();

            if(drugStoreRepository.existsByHpid(hpid)){
                if (userRepository.existsByMyDrugStore_Hpid(hpid)) {
                    throw new RuntimeException("이미 다른 사용자가 등록한 약국입니다.");
                }

                DrugStore drugStoreEntity = drugStoreRepository.findByHpid(hpid)
                        .orElseGet(() -> drugStoreRepository.save(drugStore.toEntity()));

                User user = dto.toEntity();

                user.setMyDrugStore(drugStoreEntity);

                userRepository.save(user);
            }else{
                DrugStore drugStoreEntity = drugStoreRepository.findByHpid(hpid)
                        .orElseGet(() -> drugStoreRepository.save(drugStore.toEntity()));

                User user = dto.toEntity();

                user.setMyDrugStore(drugStoreEntity);

                userRepository.save(user);
            }
        }
    }

    @Transactional
    public UserResponse login(LoginRequest dto){

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        if(!user.getPassword().equals(dto.getPassword())){
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        System.out.println("로그인 성공");

        return new UserResponse(user);
    }

    @Transactional
    public UserResponse modifyInfo(ModifyInfoRequest dto){
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if(!user.getNickname().equals(dto.getNickname()) && userRepository.existsByNickname(dto.getNickname())){
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }

        user.setNickname(dto.getNickname());

        return new UserResponse(user);
    }

    @Transactional
    public void modifyPassword(ModifyPasswordRequest dto){
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if(!user.getPassword().equals(dto.getCurrentPassword())){
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        user.setPassword(dto.getNewPassword());
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다. id=" + userId));

        userRepository.delete(user);
    }

    @Transactional
    public void updateToken(Long userId, String token) {
        log.info("토큰 업데이트 실행");
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        log.info("전달받은 토큰: {}", token);
        user.setFcmToken(token);
        log.info("토큰 업데이트 완료");
    }
}