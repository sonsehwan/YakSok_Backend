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
import com.sehwan.YakSok.user.entity.UserDevice;
import com.sehwan.YakSok.user.repository.UserDeviceRepository;
import com.sehwan.YakSok.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final DrugStoreRepository drugStoreRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signUp(UserRequest dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }

        if(userRepository.existsByNickname(dto.getNickname())) {
            throw  new RuntimeException("이미 존재하는 닉네임입니다.");
        }

        if(!dto.getRole().equals("DRUGSTORE")) {
            User user = buildUserwithEncodedPassword(dto);
            userRepository.save(user);
        }else{
            SearchDrugStoreDto drugStore = dto.getDrugStore();

            if (drugStore == null || drugStore.getHpid() == null || drugStore.getHpid().isBlank()) {
                throw new RuntimeException("약국 정보가 올바르지 않습니다.");
            }

            String hpid = drugStore.getHpid();

            if (userRepository.existsByMyDrugStore_Hpid(hpid)) {
                throw new RuntimeException("이미 다른 사용자가 등록한 약국입니다.");
            }

            DrugStore drugStoreEntity = drugStoreRepository.findByHpid(hpid)
                    .orElseGet(() -> drugStoreRepository.save(drugStore.toEntity()));

            User user = buildUserwithEncodedPassword(dto);
            user.setMyDrugStore(drugStoreEntity);
            userRepository.save(user);
        }
    }

    private User buildUserwithEncodedPassword(UserRequest dto) {
        User user = dto.toEntity();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return user;
    }

    @Transactional
    public UserResponse login(LoginRequest dto){

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        if(!passwordEncoder.matches(dto.getPassword(), user.getPassword())){
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

        if(!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())){
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(dto.getCurrentPassword()));
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다. id=" + userId));

        userRepository.delete(user);
    }

    @Transactional
    public void updateToken(Long userId, String token) {

        if(token == null || token.isBlank()){
            throw new IllegalArgumentException("등록할 FCM 토큰이 비어 있습니다.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        userDeviceRepository.findByToken(token).ifPresentOrElse(
                device -> {
                    device.setUser(user);
                    device.setUpdatedAt(LocalDateTime.now());
                },
                () -> userDeviceRepository.save(
                        UserDevice.builder().user(user).token(token).build()
                )
        );
        log.info("기기 토큰 등록 완료. userId = {}", userId);
    }

    @Transactional
    public void deleteToken(Long userId, String token) {
        if(token == null || token.isBlank()){
            throw new IllegalArgumentException("삭제할 FCM 토큰이 비어 있습니다.");
        }

        userDeviceRepository.deleteByUserIdAndToken(userId, token);

        log.info("기기 토큰 삭제 완료. userId = {}", userId);
    }
}