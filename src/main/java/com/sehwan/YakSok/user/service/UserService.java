package com.sehwan.YakSok.user.service;


import com.sehwan.YakSok.auth.JwtProvider;
import com.sehwan.YakSok.auth.dto.response.TokenResponse;
import com.sehwan.YakSok.chat.service.ChatService;
import com.sehwan.YakSok.drugstore.dto.SearchDrugStoreDto;
import com.sehwan.YakSok.drugstore.entity.DrugStore;
import com.sehwan.YakSok.drugstore.repository.DrugStoreRepository;
import com.sehwan.YakSok.user.dto.request.LoginRequest;
import com.sehwan.YakSok.user.dto.request.ModifyInfoRequest;
import com.sehwan.YakSok.user.dto.request.ModifyPasswordRequest;
import com.sehwan.YakSok.user.dto.request.UserRequest;
import com.sehwan.YakSok.user.dto.response.LoginResponse;
import com.sehwan.YakSok.user.dto.response.UserResponse;
import com.sehwan.YakSok.user.entity.RefreshToken;
import com.sehwan.YakSok.user.entity.User;
import com.sehwan.YakSok.user.entity.UserDevice;
import com.sehwan.YakSok.user.repository.RefreshTokenRepository;
import com.sehwan.YakSok.user.repository.UserDeviceRepository;
import com.sehwan.YakSok.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final DrugStoreRepository drugStoreRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final RefreshTokenRepository  refreshTokenRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final ChatService chatService;

    @Transactional
    public void signUp(UserRequest dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }

        if (userRepository.existsByLoginId(dto.getLoginId())) {
            throw new RuntimeException("이미 사용 중인 아이디입니다.");
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
    public LoginResponse login(LoginRequest dto){

        User user = userRepository.findByLoginId(dto.getLoginId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        if(!passwordEncoder.matches(dto.getPassword(), user.getPassword())){
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtProvider.createAccessToken(user.getId());
        String refreshToken = UUID.randomUUID().toString();

        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().plusWeeks(2))
                .build());

        return new LoginResponse(new UserResponse(user), accessToken, refreshToken);
    }

    @Transactional
    public TokenResponse reissueAccessToken(String refreshToken){
        RefreshToken saved = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(()-> new IllegalArgumentException("유효하지 않는 리프레쉬 토큰입니다."));

        if(saved.getExpiresAt().isBefore(LocalDateTime.now())){
            refreshTokenRepository.deleteByToken(refreshToken);
            throw new IllegalArgumentException("만료된 리프레쉬 토큰입니다.");
        }

        String newAccessToken = jwtProvider.createAccessToken(saved.getUser().getId());
        String newRefreshToken = UUID.randomUUID().toString();

        saved.setToken(newRefreshToken);
        saved.setExpiresAt(LocalDateTime.now().plusWeeks(2));

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String refreshToken){
        refreshTokenRepository.deleteByToken(refreshToken);
    }

    @Transactional
    public UserResponse modifyInfo(ModifyInfoRequest dto, Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if(!user.getNickname().equals(dto.getNickname()) && userRepository.existsByNickname(dto.getNickname())){
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }

        user.setNickname(dto.getNickname());

        return new UserResponse(user);
    }

    @Transactional
    public void modifyPassword(ModifyPasswordRequest dto, Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if(!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())){
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다. id=" + userId));

        // 채팅방 정리: 참여자에서 빼고, 그 방에 남은 참여자가 없으면 방/메시지까지 삭제
        chatService.handleUserWithdrawal(userId);

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
    public void deleteFcmToken(Long userId, String token) {
        if(token == null || token.isBlank()){
            throw new IllegalArgumentException("삭제할 FCM 토큰이 비어 있습니다.");
        }

        userDeviceRepository.deleteByUserIdAndToken(userId, token);

        log.info("기기 토큰 삭제 완료. userId = {}", userId);
    }
}