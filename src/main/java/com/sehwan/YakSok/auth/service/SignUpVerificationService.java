package com.sehwan.YakSok.auth.service;

import com.sehwan.YakSok.auth.EmailVerificationStore;
import com.sehwan.YakSok.auth.SignUpTokenStore;
import com.sehwan.YakSok.mail.MailService;
import com.sehwan.YakSok.mail.MailTemplate;
import com.sehwan.YakSok.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignUpVerificationService {

    private final UserRepository userRepository;
    private final EmailVerificationStore emailVerificationStore;
    private final SignUpTokenStore signUpTokenStore;
    private final MailService mailService;

    public void sendCode(String email){
        if(userRepository.existsByEmail(email)){
            throw new IllegalArgumentException("이미 사용중인 Email입니다.");
        }
        String code = emailVerificationStore.issue(EmailVerificationStore.Purpose.SIGN_UP, email);

        mailService.sendHtmlAsync(email,
                "[약속] 회원가입 Email 인증코드",
                MailTemplate.verificationCode("회원가입", code));
    }

    public String verifyCode(String email,String code){
        emailVerificationStore.verify(EmailVerificationStore.Purpose.SIGN_UP, email, code);
         return signUpTokenStore.issue(email);
    }
}
