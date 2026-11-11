package com.sehwan.YakSok.auth.service;

import com.sehwan.YakSok.auth.JwtProvider;
import com.sehwan.YakSok.auth.KakaoClient;
import com.sehwan.YakSok.auth.KakaoClient.KakaoProfile;
import com.sehwan.YakSok.auth.SignUpTokenStore;
import com.sehwan.YakSok.auth.dto.request.KakaoLoginRequest;
import com.sehwan.YakSok.auth.dto.request.KakaoSignUpRequest;
import com.sehwan.YakSok.auth.dto.response.KakaoLoginResponse;
import com.sehwan.YakSok.auth.dto.response.TokenResponse;
import com.sehwan.YakSok.drugstore.dto.SearchDrugStoreDto;
import com.sehwan.YakSok.drugstore.entity.DrugStore;
import com.sehwan.YakSok.drugstore.repository.DrugStoreRepository;
import com.sehwan.YakSok.user.dto.request.LoginRequest;
import com.sehwan.YakSok.user.dto.request.UserRequest;
import com.sehwan.YakSok.user.dto.response.LoginResponse;
import com.sehwan.YakSok.user.dto.response.UserResponse;
import com.sehwan.YakSok.user.entity.RefreshToken;
import com.sehwan.YakSok.user.entity.User;
import com.sehwan.YakSok.user.entity.UserRole;
import com.sehwan.YakSok.user.repository.RefreshTokenRepository;
import com.sehwan.YakSok.user.event.UserInfoChangedEvent;
import com.sehwan.YakSok.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

// 가입·로그인·토큰 발급 (일반 + 카카오). 회원 정보 수정·탈퇴·기기 등록은 UserService
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final DrugStoreRepository drugStoreRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final KakaoClient kakaoClient;
    private final SignUpTokenStore signUpTokenStore;
    private final ApplicationEventPublisher eventPublisher;

    private static final String KAKAO = "KAKAO";

    @Transactional
    public void signUp(UserRequest dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }

        if (userRepository.existsByLoginId(dto.getLoginId())) {
            throw new RuntimeException("이미 사용 중인 아이디입니다.");
        }

        User user = buildUserwithEncodedPassword(dto);
        if ("DRUGSTORE".equals(dto.getRole())) {
            user.setMyDrugStore(resolveDrugStore(dto.getDrugStore()));
        }

        // 중복·약국 검증을 모두 통과한 뒤에 소비: 앞 단계에서 실패해도 토큰이 살아 있어 재인증이 필요 없다
        signUpTokenStore.consume(dto.getEmailVerifyToken(), dto.getEmail());
        userRepository.save(user);
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



    @Transactional
    public KakaoLoginResponse kakaoLogin(KakaoLoginRequest dto) {
        KakaoProfile profile = kakaoClient.fetchProfile(dto.getAccessToken());

        return userRepository.findBySocialTypeAndSocialId(KAKAO, profile.id())
                .map(user -> new KakaoLoginResponse(false, issueLogin(user), null, null))
                .orElseGet(() -> {
                    // 신규 회원: 이메일 검증은 여기서 미리 해서, 가입 화면까지 간 뒤에 막히지 않게 한다
                    checkKakaoEmail(profile);
                    return new KakaoLoginResponse(true, null, profile.nickname(), profile.email());
                });
    }

    @Transactional
    public LoginResponse kakaoSignUp(KakaoSignUpRequest dto) {
        KakaoProfile profile = kakaoClient.fetchProfile(dto.getAccessToken());

        if (userRepository.findBySocialTypeAndSocialId(KAKAO, profile.id()).isPresent()) {
            throw new RuntimeException("이미 가입된 카카오 계정입니다.");
        }
        checkKakaoEmail(profile);

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

    @Transactional
    public UserResponse kakaoInterlock(Long userId, KakaoLoginRequest dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if(user.getSocialType() != null){
            throw new RuntimeException("이미 연동된 계정입니다.");
        }

        String id = kakaoClient.fetchProfile(dto.getAccessToken()).id();

        if(userRepository.findBySocialTypeAndSocialId(KAKAO, id).isPresent()){
            throw new RuntimeException("이미 다른 계정에 연결된 카카오입니다.");
        }

        user.setSocialType(KAKAO);
        user.setSocialId(id);

        eventPublisher.publishEvent(new UserInfoChangedEvent(userId));
        return new UserResponse(user);
    }

    @Transactional
    public UserResponse kakaoUnlink(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (user.getSocialType() == null) {
            throw new RuntimeException("연동된 카카오 계정이 없습니다.");
        }
        if (user.getPassword() == null) {
            throw new RuntimeException("카카오로 가입한 계정은 연동을 해제할 수 없습니다.");
        }

        user.setSocialType(null);
        user.setSocialId(null);

        eventPublisher.publishEvent(new UserInfoChangedEvent(userId));
        return new UserResponse(user);
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

    private void checkKakaoEmail(KakaoProfile profile) {
        if (profile.email() == null || !profile.emailVerified()) {
            throw new RuntimeException("인증된 카카오 계정 이메일이 필요합니다. 이메일 제공에 동의해주세요");
        }
        if (userRepository.existsByEmail(profile.email())) {
            throw new RuntimeException("아이디로 로그인 후 내 정보에서 카카오를 연결해주세요");
        }
    }
}
