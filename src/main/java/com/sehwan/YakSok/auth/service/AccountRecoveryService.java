package com.sehwan.YakSok.auth.service;

import com.sehwan.YakSok.auth.EmailVerificationStore;
import com.sehwan.YakSok.auth.EmailVerificationStore.Purpose;
import com.sehwan.YakSok.auth.PasswordResetTokenStore;
import com.sehwan.YakSok.mail.MailService;
import com.sehwan.YakSok.mail.MailTemplate;
import com.sehwan.YakSok.user.entity.User;
import com.sehwan.YakSok.user.repository.RefreshTokenRepository;
import com.sehwan.YakSok.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountRecoveryService {

    private final UserRepository userRepository;
    private final EmailVerificationStore verificationStore;
    private final PasswordResetTokenStore passwordResetTokenStore;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    // --- 아이디 찾기 ---

    public void sendFindIdCode(String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("가입된 이메일이 아닙니다."));

        String code = verificationStore.issue(Purpose.FIND_ID, email);
        mailService.sendHtmlAsync(email,
                "[약속] 아이디 찾기 인증코드",
                MailTemplate.verificationCode("아이디 찾기", code));
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
        mailService.sendHtmlAsync(email,
                "[약속] 비밀번호 재설정 인증코드",
                MailTemplate.verificationCode("비밀번호 재설정", code));
    }

    public String verifyResetPw(String loginId, String email, String code) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 이메일이 일치하지 않습니다."));
        if (!user.getEmail().equals(email)) {
            throw new IllegalArgumentException("아이디 또는 이메일이 일치하지 않습니다.");
        }

        verificationStore.verify(Purpose.RESET_PW, email, code);

        return passwordResetTokenStore.issue(user.getId());
    }

    @Transactional
    public void confirmResetPw(String resetToken, String newPassword) {
        Long userId = passwordResetTokenStore.consume(resetToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        user.setPassword(passwordEncoder.encode(newPassword));
        refreshTokenRepository.deleteByUser_Id(userId);
    }
}
