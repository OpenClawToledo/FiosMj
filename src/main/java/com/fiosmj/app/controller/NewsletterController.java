package com.fiosmj.app.controller;

import com.fiosmj.app.model.NewsletterSubscriber;
import com.fiosmj.app.repository.NewsletterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.*;

@RestController
@RequestMapping("/api/newsletter")
public class NewsletterController {

    private static final Logger log = LoggerFactory.getLogger(NewsletterController.class);

    @Autowired private NewsletterRepository repo;

    @Value("${brevo.api.key:}")
    private String brevoApiKey;

    @Value("${brevo.list.id:0}")
    private int brevoListId;

    @org.springframework.beans.factory.annotation.Autowired
    private com.fiosmj.app.security.AdminAuth adminAuth;

    private final ObjectMapper mapper = new ObjectMapper();

    /** Subscrever à newsletter */
    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String name  = body.get("name");

        if (email == null || email.isBlank() || !email.contains("@") || email.length() > 150) {
            return ResponseEntity.badRequest().body(Map.of("error", "E-mail inválido"));
        }
        // LGPD: inscrição só com consentimento explícito
        if (!"true".equals(String.valueOf(body.get("consent")))) {
            return ResponseEntity.badRequest().body(Map.of("error", "Marque a opção de aceite para receber as novidades"));
        }
        if (name != null && name.length() > 100) name = name.substring(0, 100);

        // Verificar se já existe (reativa quem tinha cancelado)
        var existing = repo.findByEmail(email.toLowerCase().trim());
        if (existing.isPresent()) {
            if (!existing.get().isActive()) {
                existing.get().setActive(true);
                repo.save(existing.get());
            }
            return ResponseEntity.ok(Map.of("success", true, "message", "Você já está inscrita! 💕"));
        }

        NewsletterSubscriber sub = new NewsletterSubscriber();
        sub.setEmail(email.toLowerCase().trim());
        sub.setName(name);
        repo.save(sub);

        // Tentar sincronizar com Brevo (se configurado)
        if (brevoApiKey != null && !brevoApiKey.isBlank()) {
            syncWithBrevo(sub);
        } else {
            log.info("Brevo não configurado — e-mail {} salvo apenas localmente", email);
        }

        return ResponseEntity.ok(Map.of("success", true,
            "message", "Inscrita com sucesso! Em breve você vai receber novidades da Fios MJ 💕🧶"));
    }

    /** Cancelar subscrição */
    @PostMapping("/unsubscribe")
    public ResponseEntity<?> unsubscribe(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        return repo.findByEmail(email.toLowerCase().trim())
            .map(sub -> {
                sub.setActive(false);
                repo.save(sub);
                return ResponseEntity.ok(Map.of("success", true, "message", "Inscrição cancelada."));
            })
            .orElse(ResponseEntity.ok(Map.of("success", true)));
    }

    /** Admin: listar subscritoras */
    @GetMapping("/admin/subscribers")
    public ResponseEntity<?> listSubscribers(@RequestHeader(value = "X-Admin-Secret", required = false) String secret) {
        if (!adminAuth.isValid(secret))
            return ResponseEntity.status(403).body(Map.of("error", "Não autorizado"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (NewsletterSubscriber s : repo.findByActiveTrueOrderByCreatedAtDesc()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", s.getId());
            m.put("email", s.getEmail());
            m.put("name", s.getName());
            m.put("createdAt", s.getCreatedAt());
            result.add(m);
        }
        return ResponseEntity.ok(Map.of("total", result.size(), "subscribers", result));
    }

    /** Admin: configurar Brevo e sincronizar lista */
    @PostMapping("/admin/configure-brevo")
    public ResponseEntity<?> configureBrevo(
            @RequestHeader(value = "X-Admin-Secret", required = false) String secret,
            @RequestBody Map<String, Object> body) {
        if (!adminAuth.isValid(secret))
            return ResponseEntity.status(403).body(Map.of("error", "Não autorizado"));

        // Testar a key
        String apiKey = (String) body.get("apiKey");
        if (apiKey == null || apiKey.isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "apiKey obrigatória"));

        try {
            java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                .uri(URI.create("https://api.brevo.com/v3/account"))
                .header("api-key", apiKey)
                .GET().build();
            HttpResponse<String> resp = HttpClient.newHttpClient()
                .send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 200) {
                return ResponseEntity.ok(Map.of("success", true,
                    "message", "API Key válida! Adiciona brevo.api.key no application.properties e reinicia.",
                    "response", mapper.readTree(resp.body()).get("email")));
            } else {
                return ResponseEntity.status(400).body(Map.of("error", "API Key inválida: " + resp.body()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    private void syncWithBrevo(NewsletterSubscriber sub) {
        try {
            Map<String, Object> contact = new LinkedHashMap<>();
            contact.put("email", sub.getEmail());
            if (sub.getName() != null)
                contact.put("attributes", Map.of("FIRSTNAME", sub.getName()));
            if (brevoListId > 0)
                contact.put("listIds", List.of(brevoListId));
            contact.put("updateEnabled", true);

            java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                .uri(URI.create("https://api.brevo.com/v3/contacts"))
                .header("api-key", brevoApiKey)
                .header("Content-Type", "application/json")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(contact)))
                .build();

            HttpResponse<String> resp = HttpClient.newHttpClient()
                .send(req, HttpResponse.BodyHandlers.ofString());

            log.info("Brevo sync para {}: status={}", sub.getEmail(), resp.statusCode());
        } catch (Exception e) {
            log.warn("Erro ao sincronizar com Brevo: {}", e.getMessage());
        }
    }
}
