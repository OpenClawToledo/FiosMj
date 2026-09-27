package com.fiosmj.app.controller;

import com.fiosmj.app.security.AdminAuth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/** Upload de imagens pelo painel admin → /uploads/{categoria}/{arquivo}. */
@RestController
@RequestMapping("/api/admin/upload")
public class AdminUploadController {

    private static final Set<String> CATEGORIES = Set.of("roupas", "amigurumi", "bolsas", "acessorios", "decoracao", "blog", "outros");
    private static final Map<String, String> TYPES = Map.of(
            "image/jpeg", ".jpg", "image/png", ".png", "image/webp", ".webp");

    private final AdminAuth adminAuth;

    @Value("${uploads.path:./data/uploads}")
    private String uploadsPath;

    public AdminUploadController(AdminAuth adminAuth) {
        this.adminAuth = adminAuth;
    }

    @PostMapping
    public ResponseEntity<?> upload(@RequestHeader(value = "X-Admin-Secret", required = false) String secret,
                                    @RequestParam("file") MultipartFile file,
                                    @RequestParam(value = "category", defaultValue = "outros") String category) throws IOException {
        if (!adminAuth.isValid(secret))
            return ResponseEntity.status(403).body(Map.of("error", "Não autorizado"));
        if (file.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "Arquivo vazio"));
        String ext = TYPES.get(file.getContentType());
        if (ext == null)
            return ResponseEntity.badRequest().body(Map.of("error", "Use JPG, PNG ou WEBP"));
        String cat = CATEGORIES.contains(category) ? category : "outros";

        String filename = UUID.randomUUID().toString().substring(0, 12) + ext;
        Path dir = Paths.get(uploadsPath, cat);
        Files.createDirectories(dir);
        file.transferTo(dir.resolve(filename).toAbsolutePath());

        return ResponseEntity.ok(Map.of("url", "/uploads/" + cat + "/" + filename));
    }
}
