package com.fiosmj.app.controller;

import com.fiosmj.app.model.ContactMessage;
import com.fiosmj.app.repository.ContactMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    @Autowired
    private ContactMessageRepository contactRepo;

    @PostMapping
    public ResponseEntity<?> submit(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String email = body.get("email");
        String message = body.get("message");

        if (name == null || name.isBlank() || message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nome e mensagem são obrigatórios"));
        }

        ContactMessage cm = new ContactMessage();
        cm.setName(name.trim());
        cm.setEmail(email != null ? email.trim() : null);
        cm.setMessage(message.trim());
        contactRepo.save(cm);

        return ResponseEntity.ok(Map.of("success", true, "message", "Mensagem enviada! Responderemos em breve 💕"));
    }
}
