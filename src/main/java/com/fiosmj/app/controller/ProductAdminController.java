package com.fiosmj.app.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiosmj.app.model.Product;
import com.fiosmj.app.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Admin CRUD de produtos — protegido por X-Admin-Secret.
 * Não requer JWT; autenticação é via header.
 */
@RestController
@RequestMapping("/api/admin/products")
public class ProductAdminController {

    private static final Set<String> AVAILABILITY =
            Set.of("PRONTA_ENTREGA", "SOB_ENCOMENDA", "SOB_CONSULTA", "INDISPONIVEL");

    private final ProductRepository productRepository;
    private final ProductController productController; // reutiliza toMap()
    private final ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Autowired
    private com.fiosmj.app.security.AdminAuth adminAuth;

    public ProductAdminController(ProductRepository productRepository,
                                  ProductController productController,
                                  ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.productController = productController;
        this.objectMapper = objectMapper;
    }

    // ─── LIST ────────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<?> listAll(@RequestHeader(value = "X-Admin-Secret", required = false) String secret) {
        if (!adminAuth.isValid(secret)) return forbidden();
        List<Map<String, Object>> result = productRepository.findAllByOrderByDisplayOrderAscIdAsc()
                .stream().map(productController::toMap).toList();
        return ResponseEntity.ok(result);
    }

    // ─── CREATE ──────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<?> create(@RequestHeader(value = "X-Admin-Secret", required = false) String secret,
                                    @RequestBody Map<String, Object> body) {
        if (!adminAuth.isValid(secret)) return forbidden();

        Product p = new Product();
        applyBody(p, body);
        productRepository.save(p);
        return ResponseEntity.ok(productController.toMap(p));
    }

    // ─── UPDATE ──────────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@RequestHeader(value = "X-Admin-Secret", required = false) String secret,
                                    @PathVariable Long id,
                                    @RequestBody Map<String, Object> body) {
        if (!adminAuth.isValid(secret)) return forbidden();

        return productRepository.findById(id).map(p -> {
            applyBody(p, body);
            productRepository.save(p);
            return ResponseEntity.ok(productController.toMap(p));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ─── TOGGLE ACTIVE ───────────────────────────────────────────────────────

    @PostMapping("/{id}/toggle")
    public ResponseEntity<?> toggle(@RequestHeader(value = "X-Admin-Secret", required = false) String secret,
                                    @PathVariable Long id) {
        if (!adminAuth.isValid(secret)) return forbidden();

        return productRepository.findById(id).map(p -> {
            p.setActive(!p.isActive());
            productRepository.save(p);
            return ResponseEntity.ok(Map.of("id", p.getId(), "active", p.isActive()));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ─── DELETE (permanente) ─────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@RequestHeader(value = "X-Admin-Secret", required = false) String secret,
                                    @PathVariable Long id) {
        if (!adminAuth.isValid(secret)) return forbidden();

        if (!productRepository.existsById(id))
            return ResponseEntity.notFound().build();

        productRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true, "deleted", id));
    }

    // ─── SEED (produtos originais) ───────────────────────────────────────────

    @PostMapping("/seed")
    public ResponseEntity<?> seed(@RequestHeader(value = "X-Admin-Secret", required = false) String secret) {
        if (!adminAuth.isValid(secret)) return forbidden();

        if (productRepository.count() > 0)
            return ResponseEntity.ok(Map.of("message", "Produtos já existem na base de dados"));

        List<Product> seeds = buildSeeds();
        productRepository.saveAll(seeds);
        return ResponseEntity.ok(Map.of("success", true, "count", seeds.size()));
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────

    private void applyBody(Product p, Map<String, Object> body) {
        if (body.containsKey("name"))        p.setName((String) body.get("name"));
        if (body.containsKey("description")) p.setDescription((String) body.get("description"));
        if (body.containsKey("imageUrl"))    p.setImageUrl((String) body.get("imageUrl"));
        if (body.containsKey("category"))    p.setCategory((String) body.get("category"));
        if (body.containsKey("active"))      p.setActive(Boolean.TRUE.equals(body.get("active")));
        if (body.containsKey("availability")) {
            Object a = body.get("availability");
            String v = a == null ? null : a.toString().trim().toUpperCase();
            p.setAvailability(v != null && AVAILABILITY.contains(v) ? v : null);
        }
        if (body.containsKey("leadTime")) {
            Object lt = body.get("leadTime");
            String v = lt == null ? null : lt.toString().trim();
            p.setLeadTime(v == null || v.isEmpty() ? null : v.substring(0, Math.min(v.length(), 60)));
        }

        if (body.containsKey("price"))
            p.setPrice(((Number) body.get("price")).doubleValue());
        if (body.containsKey("stock"))
            p.setStock(body.get("stock") != null ? ((Number) body.get("stock")).intValue() : null);
        if (body.containsKey("displayOrder"))
            p.setDisplayOrder(((Number) body.get("displayOrder")).intValue());

        // sizes: aceita List<Map> → serializa para JSON
        if (body.containsKey("sizes")) {
            Object rawSizes = body.get("sizes");
            if (rawSizes == null) {
                p.setSizesJson(null);
            } else {
                try {
                    p.setSizesJson(objectMapper.writeValueAsString(rawSizes));
                } catch (Exception ignored) {}
            }
        }
    }

    private ResponseEntity<?> forbidden() {
        return ResponseEntity.status(403).body(Map.of("error", "Não autorizado"));
    }

    /** Produtos iniciais da loja (também usados no primeiro start de um servidor novo). */
    public List<Product> buildSeeds() {
        List<Product> list = new ArrayList<>();

        list.add(seed(1, "Cropped Halter Brasil 🇧🇷",
            "Cropped de crochê com tema Copa do Mundo 2026! Feito à mão com fio 100% algodão nas cores da bandeira do Brasil. Perfeito para torcer com muito estilo! 🎉",
            100.0, "/img/cropped-brasil.jpg", "Roupas",
            "[{\"size\":\"P\",\"price\":100.0},{\"size\":\"M\",\"price\":120.0},{\"size\":\"G\",\"price\":150.0}]", 5));

        list.add(seed(2, "Amigurumi Nossa Senhora 🙏",
            "Amigurumi artesanal da Nossa Senhora Aparecida, feito com muito carinho e devoção. Detalhes em dourado, acabamento delicado. Uma peça especial para presentear ou ter em casa. ✨",
            80.0, "/img/nossa-senhora.jpg", "Amigurumi", null, 3));

        list.add(seed(3, "Caneta Decorada ✏️",
            "Caneta decorada com crochê artesanal colorido. Ótima opção de presente criativo e único! Disponível em diversas cores. 🌈",
            20.0, "/img/caneta.jpg", "Acessórios", null, 10));

        list.add(seed(4, "Bolsa de Anéis Rose 🌸",
            "Bolsa artesanal de crochê em técnica de anéis, feita com fio de polipropileno na cor rose/terracota. Forro interno de cetim rose metálico e alça hexagonal em resina âmbar. Peça única, feita à mão com muito carinho. Preço a partir de R$250 — varia conforme tamanho e modelo. Sob encomenda.",
            250.0, "/uploads/bolsas/bolsa-aneis-rose.jpg", "Bolsas", null, 1));

        list.add(seed(5, "Amigurumi Cachorrinho 🐶",
            "Amigurumi cachorrinho feito à mão com fio de algodão, acabamento delicado, olhinhos bordados e coleirinha vermelha. Uma peça única e cheia de personalidade! Preço a partir de R$120 — varia conforme tamanho e modelo. Sob encomenda.",
            120.0, "/uploads/amigurumi/amigurumi-cachorro-bege.jpg", "Amigurumi", null, 1));

        list.add(seed(6, "Bolsa Cordão Bege 🧵",
            "Bolsa artesanal feita com cordão Rayontex Bege 1200TEX 100% polipropileno — material resistente e de acabamento premium. Peça estruturada, feita à mão com muito cuidado. Preço a partir de R$250 — varia conforme tamanho e modelo. Sob encomenda.",
            250.0, "/uploads/bolsas/cordao-rayontex-bege.jpg", "Bolsas", null, 1));

        list.add(seed(7, "Amigurumi Boneca 🎀",
            "Boneca amigurumi artesanal com cabelo cacheado e vestidinho colorido feito à mão. Disponível em diversas cores e modelos. Peça perfeita para presentear! Preço a partir de R$120 — varia conforme tamanho e modelo. Sob encomenda.",
            120.0, "/uploads/amigurumi/amigurumi-bonecas.jpg", "Amigurumi", null, 1));

        list.add(seed(8, "Cropped Halter Copa 2026 🇧🇷",
            "Cropped de crochê artesanal nas cores do Brasil — amarelo com detalhes em verde e patch da bandeirinha 🇧🇷. Alças cruzadas nas costas, feito à mão com muito carinho. Perfeito para torcer com estilo na Copa 2026! Disponível nos tamanhos P, M e G.",
            100.0, "/uploads/roupas/cropped-brasil-frente.jpg", "Roupas",
            "[{\"size\":\"P\",\"price\":100.0},{\"size\":\"M\",\"price\":120.0},{\"size\":\"G\",\"price\":140.0}]", 3));

        return list;
    }

    private Product seed(int order, String name, String description, double price,
                         String imageUrl, String category, String sizesJson, int stock) {
        Product p = new Product();
        p.setDisplayOrder(order);
        p.setName(name);
        p.setDescription(description);
        p.setPrice(price);
        p.setImageUrl(imageUrl);
        p.setCategory(category);
        p.setSizesJson(sizesJson);
        p.setStock(stock);
        p.setActive(true);
        return p;
    }
}
