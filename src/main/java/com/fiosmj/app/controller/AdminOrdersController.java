package com.fiosmj.app.controller;

import com.fiosmj.app.model.ContactMessage;
import com.fiosmj.app.model.Order;
import com.fiosmj.app.model.OrderItem;
import com.fiosmj.app.repository.ContactMessageRepository;
import com.fiosmj.app.repository.OrderRepository;
import com.fiosmj.app.security.AdminAuth;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Painel admin: pedidos e mensagens de contato.
 * Protegido por X-Admin-Secret (AdminAuth), como as outras rotas /api/admin.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminOrdersController {

    private final OrderRepository orders;
    private final ContactMessageRepository messages;
    private final AdminAuth adminAuth;

    public AdminOrdersController(OrderRepository orders, ContactMessageRepository messages, AdminAuth adminAuth) {
        this.orders = orders;
        this.messages = messages;
        this.adminAuth = adminAuth;
    }

    // ─── Pedidos ─────────────────────────────────────────────────────────────

    @GetMapping("/orders")
    @Transactional(readOnly = true)
    public ResponseEntity<?> listOrders(@RequestHeader(value = "X-Admin-Secret", required = false) String secret) {
        if (!adminAuth.isValid(secret)) return forbidden();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Order o : orders.findTop200ByOrderByCreatedAtDesc()) result.add(toMap(o));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/orders/{id}/status")
    @Transactional
    public ResponseEntity<?> setStatus(@RequestHeader(value = "X-Admin-Secret", required = false) String secret,
                                       @PathVariable Long id,
                                       @RequestBody Map<String, String> body) {
        if (!adminAuth.isValid(secret)) return forbidden();
        Order.Status status;
        try {
            status = Order.Status.valueOf(String.valueOf(body.get("status")).toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Status inválido"));
        }
        return orders.findById(id).<ResponseEntity<?>>map(o -> {
            o.setStatus(status);
            orders.save(o);
            return ResponseEntity.ok(toMap(o));
        }).orElse(ResponseEntity.notFound().build());
    }

    private Map<String, Object> toMap(Order o) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", o.getId());
        m.put("status", o.getStatus() == null ? "PENDING" : o.getStatus().name());
        m.put("createdAt", o.getCreatedAt());
        m.put("totalAmount", o.getTotalAmount());
        m.put("name", o.getName());
        m.put("email", o.getEmail());
        m.put("phone", o.getPhone());
        m.put("hasAccount", o.getCustomer() != null);
        m.put("address", joinAddress(o));
        List<Map<String, Object>> items = new ArrayList<>();
        if (o.getItems() != null) {
            for (OrderItem i : o.getItems()) {
                Map<String, Object> im = new LinkedHashMap<>();
                im.put("productName", i.getProductName());
                im.put("quantity", i.getQuantity());
                im.put("price", i.getPrice());
                items.add(im);
            }
        }
        m.put("items", items);
        return m;
    }

    private static String joinAddress(Order o) {
        StringBuilder b = new StringBuilder();
        add(b, o.getStreet());
        if (o.getNumber() != null && !o.getNumber().isBlank()) b.append(", ").append(o.getNumber().trim());
        add(b, o.getComplement());
        add(b, o.getNeighborhood());
        String cityUf = (Objects.toString(o.getCity(), "").trim() + "/" + Objects.toString(o.getState(), "").trim());
        if (!cityUf.equals("/")) add(b, cityUf);
        if (o.getCep() != null && !o.getCep().isBlank()) add(b, "CEP " + o.getCep().trim());
        return b.toString();
    }

    private static void add(StringBuilder b, String v) {
        if (v == null || v.isBlank()) return;
        if (b.length() > 0) b.append(" – ");
        b.append(v.trim());
    }

    // ─── Mensagens de contato ────────────────────────────────────────────────

    @GetMapping("/messages")
    public ResponseEntity<?> listMessages(@RequestHeader(value = "X-Admin-Secret", required = false) String secret) {
        if (!adminAuth.isValid(secret)) return forbidden();
        List<Map<String, Object>> result = new ArrayList<>();
        for (ContactMessage c : messages.findTop200ByOrderByCreatedAtDesc()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("name", c.getName());
            m.put("email", c.getEmail());
            m.put("message", c.getMessage());
            m.put("createdAt", c.getCreatedAt());
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/messages/{id}")
    public ResponseEntity<?> deleteMessage(@RequestHeader(value = "X-Admin-Secret", required = false) String secret,
                                           @PathVariable Long id) {
        if (!adminAuth.isValid(secret)) return forbidden();
        if (!messages.existsById(id)) return ResponseEntity.notFound().build();
        messages.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    private static ResponseEntity<?> forbidden() {
        return ResponseEntity.status(403).body(Map.of("error", "Não autorizado"));
    }
}
