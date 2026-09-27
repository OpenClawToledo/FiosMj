package com.fiosmj.app.controller;

import com.fiosmj.app.model.BlogPost;
import com.fiosmj.app.repository.BlogPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class BlogController {

    @Autowired
    private BlogPostRepository blogRepo;

    @org.springframework.beans.factory.annotation.Autowired
    private com.fiosmj.app.security.AdminAuth adminAuth;

    @GetMapping("/api/blog")
    public List<BlogPost> listPublished() {
        return blogRepo.findByPublishedTrueOrderByCreatedAtDesc();
    }

    @GetMapping("/api/blog/{slug}")
    public ResponseEntity<?> getBySlug(@PathVariable String slug) {
        return blogRepo.findBySlug(slug)
            .filter(BlogPost::isPublished)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // ─── ADMIN (X-Admin-Secret) ──────────────────────────────────────────────

    /** Lista todos os posts, incluindo rascunhos. */
    @GetMapping("/api/admin/blog")
    public ResponseEntity<?> listAll(@RequestHeader(value = "X-Admin-Secret", required = false) String secret) {
        if (!adminAuth.isValid(secret)) return forbidden();
        return ResponseEntity.ok(blogRepo.findAllByOrderByCreatedAtDesc());
    }

    @PostMapping("/api/admin/blog")
    public ResponseEntity<?> createPost(
            @RequestHeader(value = "X-Admin-Secret", required = false) String secret,
            @RequestBody Map<String, Object> body) {
        if (!adminAuth.isValid(secret)) return forbidden();

        BlogPost post = new BlogPost();
        String error = applyBody(post, body);
        if (error != null) return ResponseEntity.badRequest().body(Map.of("error", error));
        if (blogRepo.existsBySlug(post.getSlug()))
            return ResponseEntity.badRequest().body(Map.of("error", "Já existe um post com este endereço (slug)"));
        blogRepo.save(post);
        return ResponseEntity.ok(Map.of("success", true, "id", post.getId()));
    }

    @PutMapping("/api/admin/blog/{id}")
    public ResponseEntity<?> updatePost(
            @PathVariable Long id,
            @RequestHeader(value = "X-Admin-Secret", required = false) String secret,
            @RequestBody Map<String, Object> body) {
        if (!adminAuth.isValid(secret)) return forbidden();

        Optional<BlogPost> found = blogRepo.findById(id);
        if (found.isEmpty()) return ResponseEntity.notFound().build();
        BlogPost post = found.get();
        String error = applyBody(post, body);
        if (error != null) return ResponseEntity.badRequest().body(Map.of("error", error));
        if (blogRepo.existsBySlugAndIdNot(post.getSlug(), id))
            return ResponseEntity.badRequest().body(Map.of("error", "Já existe um post com este endereço (slug)"));
        blogRepo.save(post);
        return ResponseEntity.ok(Map.of("success", true, "id", post.getId()));
    }

    @PostMapping("/api/admin/blog/{id}/toggle")
    public ResponseEntity<?> togglePublished(
            @PathVariable Long id,
            @RequestHeader(value = "X-Admin-Secret", required = false) String secret) {
        if (!adminAuth.isValid(secret)) return forbidden();
        return blogRepo.findById(id).<ResponseEntity<?>>map(p -> {
            p.setPublished(!p.isPublished());
            blogRepo.save(p);
            return ResponseEntity.ok(Map.of("success", true, "published", p.isPublished()));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/admin/blog/{id}")
    public ResponseEntity<?> deletePost(
            @PathVariable Long id,
            @RequestHeader(value = "X-Admin-Secret", required = false) String secret) {
        if (!adminAuth.isValid(secret)) return forbidden();
        if (!blogRepo.existsById(id)) return ResponseEntity.notFound().build();
        blogRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /** Preenche o post a partir do JSON; devolve mensagem de erro ou null. */
    private String applyBody(BlogPost post, Map<String, Object> body) {
        String title = str(body.get("title"));
        if (title == null) return "O título é obrigatório";
        String content = str(body.get("content"));
        if (content == null) return "O conteúdo é obrigatório";
        String slug = str(body.get("slug"));
        slug = slugify(slug != null ? slug : title);
        if (slug.isEmpty()) return "Endereço (slug) inválido";

        post.setTitle(title);
        post.setSlug(slug);
        post.setExcerpt(str(body.get("excerpt")));
        post.setContent(content);
        post.setImageUrl(str(body.get("imageUrl")));
        String author = str(body.get("authorName"));
        post.setAuthorName(author != null ? author : "Maju");
        post.setPublished(Boolean.TRUE.equals(body.get("published")));
        return null;
    }

    private static String str(Object o) {
        if (o == null) return null;
        String s = o.toString().trim();
        return s.isEmpty() ? null : s;
    }

    static String slugify(String input) {
        String s = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return s.length() > 80 ? s.substring(0, 80).replaceAll("-$", "") : s;
    }

    private static ResponseEntity<?> forbidden() {
        return ResponseEntity.status(403).body(Map.of("error", "Não autorizado"));
    }

    @PostMapping("/api/admin/blog/seed")
    public ResponseEntity<?> seedBlog(@RequestHeader(value = "X-Admin-Secret", required = false) String secret) {
        if (!adminAuth.isValid(secret))
            return ResponseEntity.status(403).body(Map.of("error", "Não autorizado"));

        if (blogRepo.count() > 0)
            return ResponseEntity.ok(Map.of("message", "Já tem posts cadastrados"));

        BlogPost p1 = new BlogPost();
        p1.setTitle("Como fazer um Amigurumi para iniciantes 🧶");
        p1.setSlug("como-fazer-amigurumi-iniciantes");
        p1.setExcerpt("Aprenda os pontos básicos e crie seu primeiro boneco de crochê! Neste tutorial, vou te ensinar passo a passo como fazer um amigurumi simples e lindo.");
        p1.setContent("""
            <h2>O que é Amigurumi?</h2>
            <p>Amigurumi é a arte japonesa de tecer ou fazer crochê de pequenas criaturas de pelúcia. O nome vem das palavras japonesas <em>ami</em> (tricô ou crochê) e <em>nuigurumi</em> (boneco de pelúcia).</p>

            <h2>Materiais necessários</h2>
            <ul>
              <li>Fio de algodão 100% na cor desejada</li>
              <li>Agulha de crochê nº 2,5 ou 3</li>
              <li>Enchimento de fibra sintética</li>
              <li>Olhinhos de segurança</li>
              <li>Agulha de lã para finalizar</li>
            </ul>

            <h2>Pontos básicos</h2>
            <p>Para o amigurumi, você precisará dominar:</p>
            <ul>
              <li><strong>Corrente (ch)</strong> — ponto base</li>
              <li><strong>Ponto baixo (pb)</strong> — mais usado no amigurumi</li>
              <li><strong>Aumento</strong> — 2 pontos baixos no mesmo ponto</li>
              <li><strong>Diminuição</strong> — 2 pontos juntos</li>
            </ul>

            <h2>Passo a passo</h2>
            <p>Comece com um anel mágico, faça 6 pontos baixos, e vá aumentando conforme o padrão. Diverta-se! 💕</p>
            """);
        p1.setImageUrl("https://images.unsplash.com/photo-1620799140188-3b2a02fd9a77?w=600");
        p1.setAuthorName("Maju");
        p1.setPublished(true);
        blogRepo.save(p1);

        BlogPost p2 = new BlogPost();
        p2.setTitle("Dicas para cuidar das suas peças de crochê ✨");
        p2.setSlug("dicas-cuidar-pecas-croche");
        p2.setExcerpt("Suas peças artesanais merecem cuidado especial! Veja como lavar, secar e guardar cada peça para durar muito mais tempo.");
        p2.setContent("""
            <h2>Como lavar peças de crochê</h2>
            <p>As peças de crochê de algodão podem ser lavadas à mão ou na máquina, mas sempre com cuidado!</p>

            <h3>Lavagem à mão (recomendada)</h3>
            <ol>
              <li>Use água fria ou morna (máx. 30°C)</li>
              <li>Sabão neutro ou específico para lã</li>
              <li>Mergulhe e agite suavemente</li>
              <li>Enxágue bem sem torcer</li>
            </ol>

            <h3>Lavagem na máquina</h3>
            <p>Use sempre o ciclo delicado, temperatura fria e coloque a peça dentro de uma fronha ou rede de proteção.</p>

            <h2>Como secar</h2>
            <p>Nunca coloque crochê na secadora! Seque sempre à sombra, deitado em uma superfície plana. Isso evita que a peça deforme.</p>

            <h2>Guardando suas peças</h2>
            <p>Guarde em local seco, de preferência dobrado (não pendurado). Para peças coloridas, evite exposição prolongada ao sol para não desbotar. 🌸</p>
            """);
        p2.setImageUrl("https://images.unsplash.com/photo-1585232351009-aa87416f5e36?w=600");
        p2.setAuthorName("Maju");
        p2.setPublished(true);
        blogRepo.save(p2);

        return ResponseEntity.ok(Map.of("success", true, "count", 2));
    }
}
