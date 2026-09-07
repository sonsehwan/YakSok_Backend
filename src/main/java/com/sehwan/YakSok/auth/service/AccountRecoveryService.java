package com.sehwan.YakSok.auth.service;

import com.sehwan.YakSok.auth.EmailVerificationStore;
import com.sehwan.YakSok.auth.EmailVerificationStore.Purpose;
import com.sehwan.YakSok.mail.MailService;
import com.sehwan.YakSok.user.entity.User;
import com.sehwan.YakSok.user.repository.RefreshTokenRepository;
import com.sehwan.YakSok.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountRecoveryService {

    private static final String TEMP_PW_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String TEMP_PW_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String TEMP_PW_DIGIT = "0123456789";
    private static final String TEMP_PW_SPECIAL = "!@#$%^&*";
    private static final int TEMP_PW_LENGTH = 12;

    private final UserRepository userRepository;
    private final EmailVerificationStore verificationStore;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    private final SecureRandom random = new SecureRandom();

    // --- 아이디 찾기 ---

    public void sendFindIdCode(String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("가입된 이메일이 아닙니다."));

        String code = verificationStore.issue(Purpose.FIND_ID, email);
        mailService.sendText(email,
                "[약속] 아이디 찾기 인증코드",
                """
                안녕하세요, 약속입니다.

                아이디 찾기 인증코드는 [%s] 입니다.
                5분 이내에 입력해주세요.

                본인이 요청하지 않았다면 이 메일을 무시하셔도 됩니다.
                """.formatted(code));
    }

    public String verifyFindId(String email, String code) {
        verificationStore.verify(Purpose.FIND_ID, email, code);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("가입된 이메일이 아닙니다."));
        return user.getLoginId();
    }

    // --- 비밀번호 재설정 ---

    public void sendResetPwCode(String loginId, String email) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 이메일이 일치하지 않습니다."));
        if (!user.getEmail().equals(email)) {
            throw new IllegalArgumentException("아이디 또는 이메일이 일치하지 않습니다.");
        }

        String code = verificationStore.issue(Purpose.RESET_PW, email);
        mailService.sendText(email,
                "[약속] 비밀번호 재설정 인증코드",
                """
                안녕하세요, 약속입니다.

                비밀번호 재설정 인증코드는 [%s] 입니다.
                5분 이내에 입력해주세요.

                본인이 요청하지 않았다면 이 메일을 무시하셔도 됩니다.
                """.formatted(code));
    }

    @Transactional
    public void verifyResetPw(String loginId, String email, String code) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 이메일이 일치하지 않습니다."));
        if (!user.getEmail().equals(email)) {
            throw new IllegalArgumentException("아이디 또는 이메일이 일치하지 않습니다.");
        }

        verificationStore.verify(Purpose.RESET_PW, email, code);

        String tempPassword = generateTempPassword();
        user.setPassword(passwordEncoder.encode(tempPassword));

        // 메일 발송이 실패하면 트랜잭션이 롤백되어 비밀번호 변경이 취소된다
        // (사용자가 임시 비밀번호를 못 받았는데 기존 비밀번호가 바뀌는 상황 방지).
        mailService.sendText(email,
                "[약속] 임시 비밀번호가 발급되었습니다",
                """
                안녕하세요, 약속입니다.

                임시 비밀번호는 [%s] 입니다.
                로그인 후 반드시 새 비밀번호로 변경해주세요.
                """.formatted(tempPassword));

        refreshTokenRepository.deleteByUser_Id(user.getId());
    }

    private String generateTempPassword() {
        List<Character> chars = new ArrayList<>();
        chars.add(pick(TEMP_PW_LOWER));
        chars.add(pick(TEMP_PW_UPPER));
        chars.add(pick(TEMP_PW_DIGIT));
        chars.add(pick(TEMP_PW_SPECIAL));

        String all = TEMP_PW_LOWER + TEMP_PW_UPPER + TEMP_PW_DIGIT + TEMP_PW_SPECIAL;
        while (chars.size() < TEMP_PW_LENGTH) {
            chars.add(pick(all));
        }

        Collections.shuffle(chars, random);

        StringBuilder sb = new StringBuilder(TEMP_PW_LENGTH);
        chars.forEach(sb::append);
        return sb.toString();
    }

    private char pick(String pool) {
        return pool.charAt(random.nextInt(pool.length()));
    }
}
