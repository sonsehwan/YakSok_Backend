package com.sehwan.YakSok.auth;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SignUpTokenStore {

    private static final long TTL_MINUTES = 10;

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    private record Entry(String email, LocalDateTime expiresAt) {}

    public synchronized String issue(String email) {
        purgeExpired();
        String token = UUID.randomUUID().toString();
        store.put(token, new Entry(email, LocalDateTime.now().plusMinutes(TTL_MINUTES)));
        return token;
    }

    public synchronized void consume(String token, String email) {
        Entry entry = store.remove(token);
        if (entry == null || entry.expiresAt().isBefore(LocalDateTime.now())
                || !entry.email().equals(email)) {
            throw new IllegalArgumentException("이메일 인증이 만료되었거나 올바르지 않습니다. 다시 인증해주세요.");
        }
    }

    private void purgeExpired() {
        LocalDateTime now = LocalDateTime.now();
        store.values().removeIf(e -> e.expiresAt().isBefore(now));
    }
}
