package com.fiosmj.app.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.util.UUID;

/**
 * Proxy de imagens do Instagram:
 * - GET /api/img/proxy?url=... → busca do CDN e serve ao browser (sem CORS)
 * - POST /api/img/download → faz download e salva em /uploads/instagram/, retorna caminho local
 * - GET /uploads/instagram/{file} → serve imagem local
 */
@RestController
public class ImageProxyController {

    private static final Logger log = LoggerFactory.getLogger(ImageProxyController.class);

    @Value("${uploads.path:./data/uploads}")
    private String uploadsPath;

    @org.springframework.beans.factory.annotation.Autowired
    private com.fiosmj.app.security.AdminAuth adminAuth;

    /** Proxy transparente — busca imagem do Instagram e repassa ao browser */
    @GetMapping("/api/img/proxy")
    public void proxyImage(@RequestParam String url, HttpServletResponse response) throws IOException {
        String host;
        try { host = URI.create(url).getHost(); } catch (Exception e) { host = null; }
        if (host == null || !(host.endsWith(".cdninstagram.com") || host.endsWith(".fbcdn.net"))) {
            response.sendError(400, "URL não permitida");
            return;
        }
        try {
            java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Referer", "https://www.instagram.com/")
                .header("Accept", "image/webp,image/apng,image/*,*/*;q=0.8")
                .GET().build();

            HttpResponse<byte[]> igResp = HttpClient.newHttpClient()
                .send(req, HttpResponse.BodyHandlers.ofByteArray());

            String contentType = igResp.headers().firstValue("content-type")
                .orElse("image/jpeg");

            response.setContentType(contentType);
            response.setHeader("Cache-Control", "public, max-age=86400");
            response.getOutputStream().write(igResp.body());
        } catch (Exception e) {
            log.warn("Proxy image error for {}: {}", url, e.getMessage());
            response.sendError(502, "Image proxy error");
        }
    }

    /** Download e salva localmente — elimina dependência do CDN */
    @PostMapping("/api/img/download")
    public ResponseEntity<?> downloadImage(@RequestHeader(value = "X-Admin-Secret", required = false) String secret,
                                           @RequestBody java.util.Map<String, String> body) {
        if (!adminAuth.isValid(secret))
            return ResponseEntity.status(403).body(java.util.Map.of("error", "Não autorizado"));
        String url = body.get("url");
        String category = body.getOrDefault("category", "instagram");
        if (url == null || url.isBlank())
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "url obrigatória"));

        try {
            java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Referer", "https://www.instagram.com/")
                .header("Accept", "image/webp,image/apng,image/*,*/*;q=0.8")
                .GET().build();

            HttpResponse<byte[]> igResp = HttpClient.newHttpClient()
                .send(req, HttpResponse.BodyHandlers.ofByteArray());

            if (igResp.statusCode() != 200)
                return ResponseEntity.status(502).body(java.util.Map.of("error", "Instagram CDN returned " + igResp.statusCode()));

            String ext = url.contains(".webp") ? ".webp" : ".jpg";
            String filename = UUID.randomUUID().toString().substring(0, 8) + ext;
            Path dir = Paths.get(uploadsPath, category);
            Files.createDirectories(dir);
            Path dest = dir.resolve(filename);
            Files.write(dest, igResp.body());

            String localUrl = "/uploads/" + category + "/" + filename;
            log.info("Downloaded Instagram image → {}", localUrl);
            return ResponseEntity.ok(java.util.Map.of("localUrl", localUrl, "filename", filename));

        } catch (Exception e) {
            log.warn("Download image error: {}", e.getMessage());
            return ResponseEntity.status(500).body(java.util.Map.of("error", e.getMessage()));
        }
    }

    /** Serve arquivos de uploads (imagens guardadas localmente) */
    @GetMapping("/uploads/{category}/{filename}")
    public ResponseEntity<Resource> serveUpload(
            @PathVariable String category,
            @PathVariable String filename) {
        Path base = Paths.get(uploadsPath).toAbsolutePath().normalize();
        Path file = base.resolve(category).resolve(filename).normalize();
        if (!file.startsWith(base) || !Files.isRegularFile(file))
            return ResponseEntity.notFound().build();

        Resource resource = new FileSystemResource(file);
        String lower = filename.toLowerCase();
        String contentType = lower.endsWith(".webp") ? "image/webp"
                : lower.endsWith(".png") ? "image/png" : "image/jpeg";
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(org.springframework.http.HttpHeaders.CACHE_CONTROL, "public, max-age=2592000")
            .body(resource);
    }
}
