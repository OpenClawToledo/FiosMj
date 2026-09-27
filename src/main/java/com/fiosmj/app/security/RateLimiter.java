package com.fiosmj.app.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiter simples em memória para proteger rotas sensíveis.
 * Limita tentativas por IP — bloqueia após MAX_ATTEMPTS em WINDOW_SECONDS.
 */
@Component
public class RateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_SECONDS = 900; // 15 minutos

    private final Map<String, AttemptRecord> attempts = new ConcurrentHashMap<>();

    public boolean isAllowed(String ip) {
        cleanup();
        AttemptRecord record = attempts.computeIfAbsent(ip, k -> new AttemptRecord());
        if (record.count >= MAX_ATTEMPTS) {
            return false;
        }
        record.count++;
        return true;
    }

    public void reset(String ip) {
        attempts.remove(ip);
    }

    private void cleanup() {
        long now = Instant.now().getEpochSecond();
        attempts.entrySet().removeIf(e -> now - e.getValue().startTime > WINDOW_SECONDS);
    }

    private static class AttemptRecord {
        int count = 0;
        long startTime = Instant.now().getEpochSecond();
    }
}
