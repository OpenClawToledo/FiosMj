package com.fiosmj.app.controller;

import com.fiosmj.app.model.*;
import com.fiosmj.app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/social")
public class SocialController {

    @Autowired private TestimonialRepository testimonialRepo;
    @Autowired private InstagramPostRepository instagramRepo;
    @Autowired private OrderRepository orderRepo;
    @Autowired private CustomerRepository customerRepo;

    @org.springframework.beans.factory.annotation.Autowired
    private com.fiosmj.app.security.AdminAuth adminAuth;

    // ─── TESTIMONIALS ────────────────────────────────────────────────────────

    /** Todos os feedbacks aprovados (ou filtrados por produto) */
    @GetMapping("/testimonials")
    public List<Map<String, Object>> getTestimonials(@RequestParam(required = false) Long productId) {
        List<Testimonial> list = productId != null
            ? testimonialRepo.findByApprovedTrueAndProductIdOrderByCreatedAtDesc(productId)
            : testimonialRepo.findByApprovedTrueOrderByCreatedAtDesc();
        return list.stream().map(this::toTestimonialMap).collect(Collectors.toList());
    }

    /** Vitrine "Presenteou com Amor" — com ocasião definida */
    @GetMapping("/gifts")
    public List<Map<String, Object>> getGifts() {
        return testimonialRepo.findByApprovedTrueAndOccasionIsNotNullOrderByCreatedAtDesc()
            .stream().map(this::toTestimonialMap).collect(Collectors.toList());
    }

    /** Enviar feedback (público, pendente aprovação) */
    @PostMapping("/testimonials")
    public ResponseEntity<?> submitTestimonial(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("customerName");
        String city = (String) body.get("city");
        String message = (String) body.get("message");
        Integer rating = body.get("rating") instanceof Number ? ((Number) body.get("rating")).intValue() : 5;

        if (name == null || name.isBlank() || message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nome e mensagem são obrigatórios"));
        }

        Testimonial t = new Testimonial();
        t.setCustomerName(name.trim());
        t.setCity(city != null ? city.trim() : null);
        t.setMessage(message.trim());
        t.setRating(rating);
        t.setOccasion((String) body.get("occasion"));
        testimonialRepo.save(t);

        return ResponseEntity.ok(Map.of("success", true, "message", "Obrigada pelo seu feedback! Aparecerá em breve 💕"));
    }

    // ─── INSTAGRAM ───────────────────────────────────────────────────────────

    /** Posts do carrossel do Instagram */
    @GetMapping("/instagram")
    public List<InstagramPost> getInstagramPosts() {
        return instagramRepo.findByActiveTrueOrderByDisplayOrderAsc();
    }

    // ─── RANKING ─────────────────────────────────────────────────────────────

    /** Ranking dos clientes mais fiéis */
    @GetMapping("/ranking")
    public List<Map<String, Object>> getRanking() {
        List<Customer> customers = customerRepo.findAll();
        List<Map<String, Object>> ranking = new ArrayList<>();

        for (Customer c : customers) {
            List<Order> orders = orderRepo.findByCustomerIdOrderByCreatedAtDesc(c.getId());
            if (orders.isEmpty()) continue;

            long orderCount = orders.size();
            double totalSpent = orders.stream().mapToDouble(Order::getTotalAmount).sum();
            String lastProduct = orders.stream()
                .max(Comparator.comparing(Order::getCreatedAt))
                .map(Order::getItemsSummary)
                .orElse("");

            Map<String, Object> entry = new LinkedHashMap<>();
            // Mostrar só primeiro nome + inicial do sobrenome por privacidade
            entry.put("displayName", maskName(c.getName()));
            entry.put("city", maskCity(c.getCity()));
            entry.put("orderCount", orderCount);
            entry.put("totalSpent", totalSpent);
            entry.put("lastProduct", lastProduct);
            entry.put("badge", getBadge(orderCount));
            ranking.add(entry);
        }

        ranking.sort((a, b) -> Long.compare((Long) b.get("orderCount"), (Long) a.get("orderCount")));
        return ranking.stream().limit(20).collect(Collectors.toList());
    }

    // ─── ADMIN ───────────────────────────────────────────────────────────────

    /** Admin: aprovar feedback */
    @PostMapping("/admin/testimonials/{id}/approve")
    public ResponseEntity<?> approveTestimonial(
            @PathVariable Long id,
            @RequestHeader(value = "X-Admin-Secret", required = false) String secret,
            @RequestBody(required = false) Map<String, Object> body) {
        if (!adminAuth.isValid(secret))
            return ResponseEntity.status(403).body(Map.of("error", "Não autorizado"));

        return testimonialRepo.findById(id).map(t -> {
            t.setApproved(true);
            if (body != null && Boolean.TRUE.equals(body.get("featured"))) t.setFeatured(true);
            if (body != null && body.get("occasion") != null) t.setOccasion((String) body.get("occasion"));
            testimonialRepo.save(t);
            return ResponseEntity.ok(Map.of("success", true));
        }).orElse(ResponseEntity.notFound().build());
    }

    /** Admin: recusar/apagar feedback */
    @DeleteMapping("/admin/testimonials/{id}")
    public ResponseEntity<?> deleteTestimonial(
            @PathVariable Long id,
            @RequestHeader(value = "X-Admin-Secret", required = false) String secret) {
        if (!adminAuth.isValid(secret))
            return ResponseEntity.status(403).body(Map.of("error", "Não autorizado"));
        if (!testimonialRepo.existsById(id)) return ResponseEntity.notFound().build();
        testimonialRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /** Admin: listar feedbacks pendentes */
    @GetMapping("/admin/testimonials/pending")
    public ResponseEntity<?> pendingTestimonials(@RequestHeader(value = "X-Admin-Secret", required = false) String secret) {
        if (!adminAuth.isValid(secret))
            return ResponseEntity.status(403).body(Map.of("error", "Não autorizado"));
        return ResponseEntity.ok(testimonialRepo.findByApprovedFalseOrderByCreatedAtAsc()
            .stream().map(this::toTestimonialMap).collect(Collectors.toList()));
    }

    /** Admin: adicionar post do Instagram */
    @PostMapping("/admin/instagram")
    public ResponseEntity<?> addInstagramPost(
            @RequestHeader(value = "X-Admin-Secret", required = false) String secret,
            @RequestBody Map<String, Object> body) {
        if (!adminAuth.isValid(secret))
            return ResponseEntity.status(403).body(Map.of("error", "Não autorizado"));

        InstagramPost post = new InstagramPost();
        post.setImageUrl((String) body.get("imageUrl"));
        post.setCaption((String) body.get("caption"));
        post.setPostUrl((String) body.get("postUrl"));
        if (body.get("displayOrder") instanceof Number)
            post.setDisplayOrder(((Number) body.get("displayOrder")).intValue());
        instagramRepo.save(post);
        return ResponseEntity.ok(Map.of("success", true, "id", post.getId()));
    }

    /** Admin: seed de posts de exemplo do Instagram */
    @PostMapping("/admin/instagram/seed")
    public ResponseEntity<?> seedInstagram(@RequestHeader(value = "X-Admin-Secret", required = false) String secret) {
        if (!adminAuth.isValid(secret))
            return ResponseEntity.status(403).body(Map.of("error", "Não autorizado"));

        if (instagramRepo.count() > 0)
            return ResponseEntity.ok(Map.of("message", "Já tem posts cadastrados"));

        String[][] posts = {
            {"https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400", "🧶 Cropped Halter Brasil — Copa 2026!", "https://instagram.com/fiosmjcroche"},
            {"https://images.unsplash.com/photo-1620799140188-3b2a02fd9a77?w=400", "✨ Nossa Senhora Aparecida em crochê com muito amor", "https://instagram.com/fiosmjcroche"},
            {"https://images.unsplash.com/photo-1585232351009-aa87416f5e36?w=400", "🎨 Detalhes que fazem a diferença — crochê artesanal", "https://instagram.com/fiosmjcroche"},
            {"https://images.unsplash.com/photo-1606107557195-0e29a4b5b4aa?w=400", "🌈 Novidades chegando! Acompanhe @fiosmjcroche", "https://instagram.com/fiosmjcroche"},
            {"https://images.unsplash.com/photo-1612532275214-e4ca76d0e4d1?w=400", "💕 Feito com amor, entregue com carinho", "https://instagram.com/fiosmjcroche"},
            {"https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400", "🏆 Peças únicas para momentos únicos", "https://instagram.com/fiosmjcroche"},
        };

        for (int i = 0; i < posts.length; i++) {
            InstagramPost p = new InstagramPost();
            p.setImageUrl(posts[i][0]);
            p.setCaption(posts[i][1]);
            p.setPostUrl(posts[i][2]);
            p.setDisplayOrder(i);
            instagramRepo.save(p);
        }
        return ResponseEntity.ok(Map.of("success", true, "count", posts.length));
    }

    /** Admin: seed de depoimentos de exemplo */
    @PostMapping("/admin/testimonials/seed")
    public ResponseEntity<?> seedTestimonials(@RequestHeader(value = "X-Admin-Secret", required = false) String secret) {
        if (!adminAuth.isValid(secret))
            return ResponseEntity.status(403).body(Map.of("error", "Não autorizado"));

        if (testimonialRepo.count() > 0)
            return ResponseEntity.ok(Map.of("message", "Já tem depoimentos cadastrados"));

        Object[][] seeds = {
            {"Ana S.", "Belo Horizonte", 5, "Amei o Cropped Halter! Chegou super bem embalado e a qualidade é incrível. Já quero mais! 😍", null},
            {"Fernanda R.", "São Paulo", 5, "Comprei o Amigurumi da Nossa Senhora de presente pra minha mãe. Ela chorou de emoção! Trabalho lindo demais. 🙏", "Dia das Mães"},
            {"Carla M.", "Uberlândia", 5, "A Maju é uma artista! A caneta decorada é delicada e sofisticada. Recomendo demais!", null},
            {"Joana P.", "Curitiba", 5, "Presente de Natal perfeito! Minha filha adorou o amigurumi, disse que era o presente mais especial que já ganhou. 🎄", "Natal"},
            {"Priscila T.", "Rio de Janeiro", 5, "Qualidade impecável, entrega rápida e ainda veio com um bilhetinho carinhoso. Voltarei sempre! 💕", null},
        };

        for (Object[] s : seeds) {
            Testimonial t = new Testimonial();
            t.setCustomerName((String) s[0]);
            t.setCity((String) s[1]);
            t.setRating((Integer) s[2]);
            t.setMessage((String) s[3]);
            t.setOccasion((String) s[4]);
            t.setApproved(true);
            t.setFeatured(s[4] != null);
            testimonialRepo.save(t);
        }
        return ResponseEntity.ok(Map.of("success", true, "count", seeds.length));
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────

    private Map<String, Object> toTestimonialMap(Testimonial t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId());
        m.put("customerName", t.getCustomerName());
        m.put("city", t.getCity());
        m.put("rating", t.getRating());
        m.put("message", t.getMessage());
        m.put("imageUrl", t.getImageUrl());
        m.put("occasion", t.getOccasion());
        m.put("featured", t.isFeatured());
        m.put("productId", t.getProductId());
        m.put("createdAt", t.getCreatedAt());
        return m;
    }

    private String maskName(String name) {
        if (name == null || name.isBlank()) return "Cliente";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0];
        return parts[0] + " " + parts[1].charAt(0) + ".";
    }

    private String maskCity(String city) {
        return city != null ? city : "Brasil";
    }

    private String getBadge(long orderCount) {
        if (orderCount >= 5) return "🥇 Ouro";
        if (orderCount >= 3) return "🥈 Prata";
        return "🥉 Bronze";
    }
}
