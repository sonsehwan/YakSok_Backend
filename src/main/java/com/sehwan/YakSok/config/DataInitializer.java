package com.sehwan.YakSok.config;

import com.sehwan.YakSok.user.entity.FriendRelation;
import com.sehwan.YakSok.user.entity.FriendRequest;
import com.sehwan.YakSok.user.entity.User;
import com.sehwan.YakSok.user.entity.UserRole;
import com.sehwan.YakSok.user.repository.FriendRelationRepository;
import com.sehwan.YakSok.user.repository.FriendRequestRepository;
import com.sehwan.YakSok.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 개발용 목 데이터 시더.
 * - 일반 유저 4명 생성 (test1~test4@yaksok.com / 비밀번호 1234)
 * - 4명 전원이 서로 친구 관계
 *
 * users 테이블이 비어 있을 때만 동작하므로 ddl-auto 가 create 든 update 든 안전하다.
 * 끄고 싶으면 application.yaml 에 app.seed.enabled: false 추가.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final FriendRelationRepository friendRelationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            log.info("[DataInitializer] 이미 유저 데이터가 존재하여 목 데이터 생성을 건너뜁니다.");
            return;
        }

        List<User> users = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            users.add(User.builder()
                    .email("user" + i + "@naver.com")
                    .loginId("testuser" + i)
                    .password(passwordEncoder.encode("user1234@"))
                    .nickname("테스트유저" + i)
                    .gender(i % 2 == 0 ? "여성" : "남성")
                    .birthdate(LocalDate.of(1995, 1, i))
                    .role(UserRole.NORMAL)
                    .penaltyEnable(false)
                    .isLocked(false)
                    .build());
        }
        userRepository.saveAll(users);

        // 모든 쌍을 서로 친구로 연결 (수락된 친구 요청 + 양방향 친구 관계)
        for (int a = 0; a < users.size(); a++) {
            for (int b = a + 1; b < users.size(); b++) {
                User u1 = users.get(a);
                User u2 = users.get(b);

                FriendRequest request = FriendRequest.create(u1, u2);
                request.accept();
                friendRequestRepository.save(request);

                friendRelationRepository.save(FriendRelation.create(u1, u2));
                friendRelationRepository.save(FriendRelation.create(u2, u1));
            }
        }

        log.info("[DataInitializer] 목 데이터 생성 완료: 유저 {}명, 전원 친구 관계", users.size());
    }
}
