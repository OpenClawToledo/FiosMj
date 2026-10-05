package com.fiosmj.app.controller;

import com.fiosmj.app.model.BlogPost;
import com.fiosmj.app.model.Product;
import com.fiosmj.app.repository.BlogPostRepository;
import com.fiosmj.app.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SEO: sitemap.xml gerado a partir do banco e páginas compartilháveis
 * (/produto/{id}, /blog/{slug}) com título, descrição e foto próprios.
 *
 * As páginas devolvem o index.html do Vue com as meta tags trocadas, então
 * WhatsApp, Instagram, Facebook e Google leem a foto e o texto certos,
 * e a cliente cai direto no produto ou post.
 */
@RestController
public class SeoController {

    private static final String DEFAULT_IMAGE = "/img/logo-fiosmj.jpg";
    private static final Pattern DATE_PREFIX = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2})");

    private final ProductRepository products;
    private final BlogPostRepository posts;

    private String baseUrl;

    @Value("${site.base-url:https://fiosmj.com}")
    void setBaseUrl(String v) { this.baseUrl = v.replaceAll("/+$", ""); }

    public SeoController(ProductRepository products, BlogPostRepository posts) {
        this.products = products;
        this.posts = posts;
    }

    // ─── sitemap.xml ─────────────────────────────────────────────────────────

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> sitemap() {
        StringBuilder xml = new StringBuilder("""
                <?xml version="1.0" encoding="UTF-8"?>
                <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
                """);
        url(xml, "/", null, "daily", "1.0");
        for (Product p : products.findByActiveTrueOrderByDisplayOrderAscIdAsc()) {
            url(xml, "/produto/" + p.getId(), date(p.getUpdatedAt()), "weekly", "0.8");
        }
        for (BlogPost b : posts.findByPublishedTrueOrderByCreatedAtDesc()) {
            if (b.getSlug() == null || b.getSlug().isBlank()) continue;
            url(xml, "/blog/" + b.getSlug(), date(b.getCreatedAt()), "monthly", "0.7");
        }
        url(xml, "/contacto", null, "yearly", "0.3");
        url(xml, "/politica-de-privacidade", null, "yearly", "0.2");
        url(xml, "/termos-e-condicoes", null, "yearly", "0.2");
        xml.append("</urlset>\n");
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .contentType(MediaType.APPLICATION_XML)
                .body(xml.toString());
    }

    private void url(StringBuilder xml, String path, String lastmod, String freq, String priority) {
        xml.append("  <url>\n    <loc>").append(xmlEsc(baseUrl + path)).append("</loc>\n");
        if (lastmod != null) xml.append("    <lastmod>").append(lastmod).append("</lastmod>\n");
        xml.append("    <changefreq>").append(freq).append("</changefreq>\n")
           .append("    <priority>").append(priority).append("</priority>\n  </url>\n");
    }

    private static String date(Object dt) {
        if (dt == null) return null;
        Matcher m = DATE_PREFIX.matcher(dt.toString());
        return m.find() ? m.group(1) : LocalDate.now().format(DateTimeFormatter.ISO_DATE);
    }

    // ─── Páginas compartilháveis ─────────────────────────────────────────────

    @GetMapping("/produto/{id}")
    public ResponseEntity<String> product(@PathVariable Long id) throws IOException {
        return products.findById(id).filter(Product::isActive)
                .map(p -> page(
                        p.getName() + " | Fios MJ Crochê Artesanal",
                        summary(p.getDescription()),
                        p.getImageUrl(),
                        "/produto/" + p.getId(),
                        "product"))
                .orElseGet(this::home);
    }

    @GetMapping("/blog/{slug}")
    public ResponseEntity<String> blogPost(@PathVariable String slug) throws IOException {
        return posts.findBySlug(slug).filter(BlogPost::isPublished)
                .map(b -> page(
                        b.getTitle() + " | Blog Fios MJ",
                        summary(b.getExcerpt() != null && !b.getExcerpt().isBlank() ? b.getExcerpt() : b.getContent()),
                        b.getImageUrl(),
                        "/blog/" + b.getSlug(),
                        "article"))
                .orElseGet(this::home);
    }

    private ResponseEntity<String> home() {
        return page(null, null, null, "/", "website");
    }

    private ResponseEntity<String> page(String title, String description, String image, String path, String type) {
        String html;
        try {
            html = new String(new ClassPathResource("static/index.html").getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            return ResponseEntity.status(302).header("Location", "/").build();
        }
        String img = absolute(image != null && !image.isBlank() ? image : DEFAULT_IMAGE);
        String url = baseUrl + path;

        if (title != null) {
            html = html.replaceFirst("(?s)<title>.*?</title>", "<title>" + quote(esc(title)) + "</title>");
            html = setMeta(html, "property", "og:title", title);
        }
        if (description != null) {
            html = setMeta(html, "name", "description", description);
            html = setMeta(html, "property", "og:description", description);
        }
        html = setMeta(html, "property", "og:type", type);
        html = setMeta(html, "property", "og:url", url);
        html = setMeta(html, "property", "og:image", img);
        html = html.replaceFirst("(?s)<link rel=\"canonical\"[^>]*>", "");
        html = html.replace("</head>", "    <link rel=\"canonical\" href=\"" + esc(url) + "\" />\n  </head>");

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES))
                .contentType(new MediaType("text", "html", StandardCharsets.UTF_8))
                .body(html);
    }

    /** Troca o content de uma meta tag existente ou cria uma nova antes de </head>. */
    private static String setMeta(String html, String attr, String key, String value) {
        String tag = "<meta " + attr + "=\"" + key + "\" content=\"" + esc(value) + "\" />";
        Pattern p = Pattern.compile("<meta\\s+" + attr + "=\"" + Pattern.quote(key) + "\"[^>]*>");
        Matcher m = p.matcher(html);
        if (m.find()) return m.replaceFirst(quote(tag));
        return html.replace("</head>", "    " + tag + "\n  </head>");
    }

    private String absolute(String path) {
        if (path.startsWith("http://") || path.startsWith("https://")) return path;
        return baseUrl + (path.startsWith("/") ? path : "/" + path);
    }

    private static String summary(String text) {
        if (text == null) return "Crochê artesanal feito à mão por mãe e filha.";
        String plain = text.replaceAll("<[^>]+>", " ").replaceAll("[#*_>`-]+", " ")
                .replaceAll("\\s+", " ").trim();
        return plain.length() <= 160 ? plain : plain.substring(0, 157).replaceAll("\\s+\\S*$", "") + "…";
    }

    private static String quote(String s) { return Matcher.quoteReplacement(s); }

    private static String esc(String v) {
        return v.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String xmlEsc(String v) {
        return v.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
