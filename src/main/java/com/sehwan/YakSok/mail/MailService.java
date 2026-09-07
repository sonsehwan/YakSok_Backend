package com.sehwan.YakSok.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String from;

    public void sendHtml(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            if (from != null && !from.isBlank()) {
                helper.setFrom(from);
            }
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true); // true = HTML 본문

            mailSender.send(message);
            log.info("메일 발송 완료. to={}, subject={}", to, subject);
        } catch (MessagingException | MailException e) {
            throw new IllegalStateException("메일 발송에 실패했습니다.", e);
        }
    }

    @Async("mailExecutor")
    public void sendHtmlAsync(String to, String subject, String html) {
        try {
            sendHtml(to, subject, html);
        } catch (Exception e) {
            log.error("비동기 메일 발송 실패. to={}, subject={}", to, subject, e);
        }
    }
}
