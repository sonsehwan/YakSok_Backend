package com.sehwan.YakSok.user.service;


import com.sehwan.YakSok.auth.JwtProvider;
import com.sehwan.YakSok.auth.KakaoClient;
import com.sehwan.YakSok.auth.KakaoClient.KakaoProfile;
import com.sehwan.YakSok.auth.dto.request.KakaoLoginRequest;
import com.sehwan.YakSok.auth.dto.request.KakaoSignUpRequest;
import com.sehwan.YakSok.auth.dto.response.KakaoLoginResponse;
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
import com.sehwan.YakSok.user.entity.UserRole;
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
    private final KakaoClient kakaoClient;

    private static final String KAKAO = "KAKAO";

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

        User user = buildUserwithEncodedPassword(dto);
        if ("DRUGSTORE".equals(dto.getRole())) {
            user.setMyDrugStore(resolveDrugStore(dto.getDrugStore()));
        }
        userRepository.save(user);
    }

    // 약국 회원 가입 시 약국 정보를 검증하고 약국 엔티티를 가져온다(없으면 저장). 일반·카카오 가입이 공유한다
    private DrugStore resolveDrugStore(SearchDrugStoreDto drugStore) {
        if (drugStore == null || drugStore.getHpid() == null || drugStore.getHpid().isBlank()) {
            throw new RuntimeException("약국 정보가 올바르지 않습니다.");
        }

        String hpid = drugStore.getHpid();

        if (userRepository.existsByMyDrugStore_Hpid(hpid)) {
            throw new RuntimeException("이미 다른 사용자가 등록한 약국입니다.");
        }

        return drugStoreRepository.findByHpid(hpid)
                .orElseGet(() -> drugStoreRepository.save(drugStore.toEntity()));
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

        return issueLogin(user);
    }

    private LoginResponse issueLogin(User user) {
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
    public KakaoLoginResponse kakaoLogin(KakaoLoginRequest dto) {
        KakaoProfile profile = kakaoClient.fetchProfile(dto.getAccessToken());

        return userRepository.findBySocialTypeAndSocialId(KAKAO, profile.id())
                .map(user -> new KakaoLoginResponse(false, issueLogin(user), null))
                .orElseGet(() -> {
                    // 신규 회원: 이메일 검증은 여기서 미리 해서, 가입 화면까지 간 뒤에 막히지 않게 한다
                    checkKakaoEmail(profile);
                    return new KakaoLoginResponse(true, null, profile.nickname());
                });
    }

    @Transactional
    public LoginResponse kakaoSignUp(KakaoSignUpRequest dto) {
        KakaoProfile profile = kakaoClient.fetchProfile(dto.getAccessToken());

        if (userRepository.findBySocialTypeAndSocialId(KAKAO, profile.id()).isPresent()) {
            throw new RuntimeException("이미 가입된 카카오 계정입니다.");
        }
        checkKakaoEmail(profile);

        if (userRepository.existsByNickname(dto.getNickname())) {
            throw new RuntimeException("이미 존재하는 닉네임입니다.");
        }

        boolean drugStore = "DRUGSTORE".equals(dto.getRole());
        User user = User.builder()
                .email(profile.email())
                .socialType(KAKAO)
                .socialId(profile.id())
                .nickname(dto.getNickname())
                .role(drugStore ? UserRole.DRUGSTORE : UserRole.NORMAL)
                .build();
        if (drugStore) {
            user.setMyDrugStore(resolveDrugStore(dto.getDrugStore()));
        }
        userRepository.save(user);

        return issueLogin(user);
    }

    // 이메일은 필수 동의 + 카카오가 인증한 것이어야 한다. 아니면 남의 이메일을 인증 없이 단 계정이 그 이메일을 선점할 수 있다
    private void checkKakaoEmail(KakaoProfile profile) {
        if (profile.email() == null || !profile.emailVerified()) {
            throw new RuntimeException("인증된 카카오 계정 이메일이 필요합니다. 이메일 제공에 동의해주세요.");
        }
        if (userRepository.existsByEmail(profile.email())) {
            throw new RuntimeException("이미 가입된 이메일입니다. 아이디로 로그인한 뒤 설정에서 카카오를 연결해주세요.");
        }
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

        if(user.getPassword() == null){
            throw new IllegalArgumentException("카카오 계정은 비밀번호를 변경할 수 없습니다.");
        }

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
    public UserResponse updateNotificationSetting(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        user.setNotificationEnabled(enabled);

        return new UserResponse(user);
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