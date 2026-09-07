package com.sehwan.YakSok.auth;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EmailVerificationStore {

    public enum Purpose { FIND_ID, RESET_PW }

    private static final int CODE_LENGTH = 6;
    private static final long TTL_MINUTES = 5;
    private static final long RESEND_BLOCK_SECONDS = 60;
    private static final int MAX_ATTEMPTS = 5;

    private final SecureRandom random = new SecureRandom();
    private final Map<Key, Entry> store = new ConcurrentHashMap<>();

    private record Key(Purpose purpose, String email) {}

    private static final class Entry {
        String code;
        LocalDateTime expiresAt;
        int attempts;
        LocalDateTime lastSentAt;

        Entry(String code, LocalDateTime expiresAt, LocalDateTime lastSentAt) {
            this.code = code;
            this.expiresAt = expiresAt;
            this.attempts = 0;
            this.lastSentAt = lastSentAt;
        }
    }

    public synchronized String issue(Purpose purpose, String email) {
        purgeExpired();

        Key key = new Key(purpose, email);
        Entry existing = store.get(key);
        if (existing != null
                && existing.lastSentAt.plusSeconds(RESEND_BLOCK_SECONDS).isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("인증코드를 방금 발송했습니다. 잠시 후 다시 시도해주세요.");
        }

        LocalDateTime now = LocalDateTime.now();
        String code = generateCode();
        store.put(key, new Entry(code, now.plusMinutes(TTL_MINUTES), now));
        return code;
    }

    public synchronized void verify(Purpose purpose, String email, String code) {
        Key key = new Key(purpose, email);
        Entry entry = store.get(key);

        if (entry == null || entry.expiresAt.isBefore(LocalDateTime.now())) {
            store.remove(key);
            throw new IllegalArgumentException("인증코드가 만료되었거나 발급되지 않았습니다. 다시 요청해주세요.");
        }

        if (entry.attempts >= MAX_ATTEMPTS) {
            store.remove(key);
            throw new IllegalArgumentException("인증 시도 횟수를 초과했습니다. 코드를 다시 요청해주세요.");
        }

        if (!entry.code.equals(code)) {
            entry.attempts++;
            throw new IllegalArgumentException("인증코드가 일치하지 않습니다.");
        }

        store.remove(key);
    }

    private void purgeExpired() {
        LocalDateTime now = LocalDateTime.now();
        store.values().removeIf(e -> e.expiresAt.isBefore(now));
    }

    private String generateCode() {
        int bound = (int) Math.pow(10, CODE_LENGTH);
        int origin = bound / 10;
        return String.valueOf(origin + random.nextInt(bound - origin));
    }
}
