package com.sehwan.YakSok.auth;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 비밀번호 재설정 허가 토큰 저장소.
 *
 * 인증코드 검증(reset-password/verify)에 성공하면 1회용 토큰을 발급하고,
 * 새 비밀번호 확정(reset-password/confirm) 때 이 토큰으로만 변경을 허용한다.
 * 로그인 전 상태에서 "이 사람은 방금 이메일 인증을 통과했다"를 짧게 증명하는 용도라
 * {@link EmailVerificationStore} 와 같은 인메모리 방식으로 충분하다 (TTL 3분, 1회용).
 */
@Component
public class PasswordResetTokenStore {

    private static final long TTL_MINUTES = 3;

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    private record Entry(Long userId, LocalDateTime expiresAt) {}

    public synchronized String issue(Long userId) {
        purgeExpired();
        String token = UUID.randomUUID().toString();
        store.put(token, new Entry(userId, LocalDateTime.now().plusMinutes(TTL_MINUTES)));
        return token;
    }

    /** 토큰을 검증하고 즉시 소비한다(1회용). 만료·없음이면 예외. */
    public synchronized Long consume(String token) {
        Entry entry = store.remove(token);
        if (entry == null || entry.expiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("비밀번호 재설정 요청이 만료되었습니다. 처음부터 다시 시도해주세요.");
        }
        return entry.userId();
    }

    private void purgeExpired() {
        LocalDateTime now = LocalDateTime.now();
        store.values().removeIf(e -> e.expiresAt().isBefore(now));
    }
}
