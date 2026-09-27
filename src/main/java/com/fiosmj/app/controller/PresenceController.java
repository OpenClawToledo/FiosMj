package com.fiosmj.app.controller;

import com.fiosmj.app.model.Order;
import com.fiosmj.app.repository.OrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/presence")
public class PresenceController {

    private final OrderRepository orderRepository;

    // In-memory presence store: sessionId -> visitor info
    private static final ConcurrentHashMap<String, VisitorInfo> activeVisitors = new ConcurrentHashMap<>();
    private static final long EXPIRY_MS = 3 * 60 * 1000; // 3 min

    public PresenceController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @PostMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping(@RequestBody Map<String, String> body) {
        String sessionId = body.getOrDefault("sessionId", UUID.randomUUID().toString());
        String displayName = body.getOrDefault("displayName", null);
        String color = body.getOrDefault("color", randomColor());

        // Cleanup expired
        long now = Instant.now().toEpochMilli();
        activeVisitors.entrySet().removeIf(e -> now - e.getValue().timestamp > EXPIRY_MS);

        // Upsert
        VisitorInfo info = activeVisitors.getOrDefault(sessionId, new VisitorInfo());
        info.sessionId = sessionId;
        info.displayName = displayName;
        info.color = info.color != null ? info.color : color;
        info.timestamp = now;
        activeVisitors.put(sessionId, info);

        long activeCount = activeVisitors.size();
        return ResponseEntity.ok(Map.of("activeCount", activeCount, "sessionId", sessionId));
    }

    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActive() {
        long now = Instant.now().toEpochMilli();
        activeVisitors.entrySet().removeIf(e -> now - e.getValue().timestamp > EXPIRY_MS);

        List<Map<String, String>> visitors = activeVisitors.values().stream()
            .sorted(Comparator.comparingLong(v -> -v.timestamp))
            .limit(8)
            .map(v -> {
                Map<String, String> m = new HashMap<>();
                m.put("sessionId", v.sessionId);
                m.put("initial", v.displayName != null && !v.displayName.isBlank()
                    ? String.valueOf(v.displayName.trim().charAt(0)).toUpperCase()
                    : "👀");
                m.put("label", v.displayName != null && !v.displayName.isBlank()
                    ? maskName(v.displayName) + " está a ver"
                    : "Alguém está a ver");
                m.put("color", v.color);
                return m;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
            "count", activeVisitors.size(),
            "visitors", visitors
        ));
    }

    @GetMapping("/recent-purchases")
    public ResponseEntity<List<Map<String, String>>> recentPurchases() {
        java.time.LocalDateTime since = java.time.LocalDateTime.now().minusHours(6);
        List<Map<String, String>> result = orderRepository.findAll().stream()
            .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(since))
            .filter(o -> "PAID".equals(o.getStatus()) || "CONFIRMED".equals(o.getStatus()))
            .sorted(Comparator.comparing(com.fiosmj.app.model.Order::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(5)
            .map(o -> {
                Map<String, String> m = new HashMap<>();
                String custName = o.getCustomer() != null && o.getCustomer().getName() != null
                    ? o.getCustomer().getName() : (o.getName() != null ? o.getName() : "Alguém");
                String customer = maskName(custName);
                String product = o.getItems() != null && !o.getItems().isEmpty()
                    ? o.getItems().get(0).getProductName()
                    : "uma peça";
                m.put("label", "🛍️ " + customer + " comprou " + product);
                long epochMs = o.getCreatedAt()
                    .atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
                m.put("ago", timeAgo(epochMs));
                return m;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ── helpers ──────────────────────────────────────────────

    private String maskName(String name) {
        if (name == null || name.isBlank()) return "Alguém";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0];
        return parts[0] + " " + parts[parts.length - 1].charAt(0) + ".";
    }

    private String timeAgo(long epochMs) {
        long diff = Instant.now().toEpochMilli() - epochMs;
        long min = diff / 60000;
        if (min < 1) return "agora mesmo";
        if (min == 1) return "há 1 minuto";
        if (min < 60) return "há " + min + " minutos";
        long h = min / 60;
        return h == 1 ? "há 1 hora" : "há " + h + " horas";
    }

    private String randomColor() {
        String[] colors = {"#e91e7b", "#9c27b0", "#f06292", "#ad1457", "#c2185b", "#d81b60", "#7b1fa2", "#e040fb"};
        return colors[new Random().nextInt(colors.length)];
    }

    static class VisitorInfo {
        String sessionId;
        String displayName;
        String color;
        long timestamp;
    }
}
