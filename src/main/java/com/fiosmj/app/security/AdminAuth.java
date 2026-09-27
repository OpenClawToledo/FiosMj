package com.fiosmj.app.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Validação da senha do painel admin (header X-Admin-Secret).
 * Sem valor padrão: se ADMIN_SECRET não estiver definido, o admin fica bloqueado.
 */
@Component
public class AdminAuth {

    private final byte[] expected;

    public AdminAuth(@Value("${admin.secret:}") String adminSecret) {
        boolean invalid = adminSecret == null || adminSecret.isBlank()
                || adminSecret.startsWith("CHANGE_ME") || adminSecret.length() < 12;
        this.expected = invalid ? null : adminSecret.getBytes(StandardCharsets.UTF_8);
    }

    public boolean isValid(String provided) {
        if (expected == null || provided == null) return false;
        return MessageDigest.isEqual(expected, provided.getBytes(StandardCharsets.UTF_8));
    }
}
