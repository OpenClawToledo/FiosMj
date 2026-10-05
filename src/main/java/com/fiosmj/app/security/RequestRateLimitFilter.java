package com.fiosmj.app.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limite de requisições por IP feito no próprio app, para quando não há Nginx na frente
 * (ex.: computador em casa com Cloudflare Tunnel). Com Nginx, os dois limites somam.
 *
 *  - Painel (/api/admin): 10 senhas erradas em 15 min bloqueiam o IP por 15 min.
 *  - Checkout, contato, newsletter, cadastro e login: 10 por minuto por IP.
 *  - Resto da API: 120 por minuto por IP.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestRateLimitFilter extends OncePerRequestFilter {

    private static final long MINUTE = 60_000L;
    private static final long ADMIN_WINDOW = 15 * MINUTE;
    private static final int ADMIN_MAX_FAILS = 10;
    private static final int SENSITIVE_PER_MIN = 10;
    private static final int API_PER_MIN = 120;
    private static final int MAX_TRACKED = 20_000;

    private final Map<String, Window> counters = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String path = req.getRequestURI();
        String ip = clientIp(req);
        long now = System.currentTimeMillis();
        if (counters.size() > MAX_TRACKED) counters.entrySet().removeIf(e -> e.getValue().expired(now));

        boolean admin = path.startsWith("/api/admin") || path.startsWith("/api/newsletter/admin")
                || path.startsWith("/api/social/admin");

        if (admin) {
            Window fails = counters.get("adminfail:" + ip);
            if (fails != null && !fails.expired(now) && fails.count >= ADMIN_MAX_FAILS) {
                tooMany(res, "Muitas tentativas no painel. Tente de novo em 15 minutos.");
                return;
            }
            chain.doFilter(req, res);
            if (res.getStatus() == 403) {
                counters.compute("adminfail:" + ip, (k, w) -> w == null || w.expired(now) ? new Window(now, ADMIN_WINDOW) : w).count++;
            }
            return;
        }

        boolean sensitive = path.startsWith("/api/checkout/preference") || path.startsWith("/api/contact")
                || path.startsWith("/api/newsletter/subscribe") || path.startsWith("/api/auth/login")
                || path.startsWith("/api/auth/register");
        // O webhook do Mercado Pago não entra no limite
        if (path.startsWith("/api/checkout/webhook")) { chain.doFilter(req, res); return; }

        String key = (sensitive ? "s:" : "a:") + ip;
        int limit = sensitive ? SENSITIVE_PER_MIN : API_PER_MIN;
        Window w = counters.compute(key, (k, old) -> old == null || old.expired(now) ? new Window(now, MINUTE) : old);
        if (++w.count > limit) {
            tooMany(res, "Muitas requisições seguidas. Aguarde um minuto e tente de novo.");
            return;
        }
        chain.doFilter(req, res);
    }

    /** IP real da visitante: o Cloudflare manda em CF-Connecting-IP; só confiamos nele vindo de rede local. */
    static String clientIp(HttpServletRequest req) {
        String remote = req.getRemoteAddr();
        String cf = req.getHeader("CF-Connecting-IP");
        if (cf != null && !cf.isBlank() && isPrivate(remote)) return cf.trim();
        return remote;
    }

    private static boolean isPrivate(String ip) {
        if (ip == null) return false;
        return ip.startsWith("127.") || ip.startsWith("10.") || ip.startsWith("192.168.") || ip.equals("::1")
                || ip.startsWith("0:0:0:0:0:0:0:1") || ip.matches("^172\\.(1[6-9]|2\\d|3[01])\\..*");
    }

    private static void tooMany(HttpServletResponse res, String msg) throws IOException {
        res.setStatus(429);
        res.setHeader("Retry-After", "60");
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"error\":\"" + msg + "\"}");
    }

    private static final class Window {
        final long start;
        final long length;
        int count;
        Window(long start, long length) { this.start = start; this.length = length; }
        boolean expired(long now) { return now - start > length; }
    }
}
