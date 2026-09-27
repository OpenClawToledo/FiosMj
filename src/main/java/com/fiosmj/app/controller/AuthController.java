package com.fiosmj.app.controller;

import com.fiosmj.app.model.Customer;
import com.fiosmj.app.repository.CustomerRepository;
import com.fiosmj.app.security.JwtUtil;
import com.fiosmj.app.security.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final CustomerRepository customerRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final RateLimiter rateLimiter;

    public AuthController(CustomerRepository customerRepository, JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder, RateLimiter rateLimiter) {
        this.customerRepository = customerRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String email = body.get("email");
        String phone = body.get("phone");
        String password = body.get("password");

        if (name == null || email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "name, email e password são obrigatórios"));
        }

        if (customerRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "E-mail já cadastrado"));
        }

        Customer customer = new Customer();
        customer.setName(name);
        customer.setEmail(email.toLowerCase().trim());
        customer.setPhone(phone);
        customer.setPasswordHash(passwordEncoder.encode(password));
        customerRepository.save(customer);

        String token = jwtUtil.generateToken(customer.getEmail());
        return ResponseEntity.ok(buildResponse(token, customer));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String ip = request.getRemoteAddr();

        // Rate limiting — máximo 5 tentativas por IP em 15 minutos
        if (!rateLimiter.isAllowed(ip)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "Demasiadas tentativas. Tente novamente em 15 minutos."));
        }

        String email = body.get("email");
        String password = body.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "email e password são obrigatórios"));
        }

        return customerRepository.findByEmail(email.toLowerCase().trim())
            .filter(c -> passwordEncoder.matches(password, c.getPasswordHash()))
            .map(c -> {
                rateLimiter.reset(ip); // login bem-sucedido — limpar contador
                c.setLastSeenAt(LocalDateTime.now());
                customerRepository.save(c);
                String token = jwtUtil.generateToken(c.getEmail());
                return ResponseEntity.ok(buildResponse(token, c));
            })
            .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "E-mail ou senha incorretos")));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal Customer customer) {
        if (customer == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Não autenticado"));
        return ResponseEntity.ok(customerToMap(customer));
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateMe(@AuthenticationPrincipal Customer customer, @RequestBody Map<String, String> body) {
        if (customer == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Não autenticado"));

        if (body.containsKey("name")) customer.setName(body.get("name"));
        if (body.containsKey("phone")) customer.setPhone(body.get("phone"));
        customerRepository.save(customer);
        return ResponseEntity.ok(customerToMap(customer));
    }

    private Map<String, Object> buildResponse(String token, Customer customer) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);
        result.put("customer", customerToMap(customer));
        return result;
    }

    public static Map<String, Object> customerToMap(Customer c) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", c.getId());
        map.put("name", c.getName());
        map.put("email", c.getEmail());
        map.put("phone", c.getPhone());
        map.put("emailNotifications", c.isEmailNotifications());
        map.put("createdAt", c.getCreatedAt());
        return map;
    }
}
