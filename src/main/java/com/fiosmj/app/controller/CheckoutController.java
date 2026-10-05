package com.fiosmj.app.controller;

import com.fiosmj.app.model.CheckoutRequest;
import com.fiosmj.app.model.Customer;
import com.fiosmj.app.model.Order;
import com.fiosmj.app.repository.CustomerRepository;
import com.fiosmj.app.repository.OrderRepository;
import com.fiosmj.app.security.JwtUtil;
import com.fiosmj.app.service.PricingService;
import com.fiosmj.app.service.PricingService.PricedItem;
import com.fiosmj.app.service.PricingService.PricingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    @Value("${mercadopago.access-token}")
    private String accessToken;

    @Value("${site.base-url:https://fiosmj.com}")
    private String siteUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final JwtUtil jwtUtil;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final PricingService pricing;

    public CheckoutController(JwtUtil jwtUtil, CustomerRepository customerRepository,
                              OrderRepository orderRepository, PricingService pricing) {
        this.jwtUtil = jwtUtil;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.pricing = pricing;
    }

    @PostMapping("/preference")
    public ResponseEntity<?> createPreference(
            @RequestBody CheckoutRequest req,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // Preços sempre do banco: o valor mandado pelo navegador é ignorado
        if (req.getItems() == null || req.getItems().isEmpty() || req.getItems().size() > 50)
            return ResponseEntity.badRequest().body(Map.of("error", "Carrinho vazio ou inválido."));
        final List<PricedItem> priced;
        try {
            priced = req.getItems().stream()
                    .map(i -> pricing.price(i.getProductId(), i.getSelectedSize(), i.getQuantity()))
                    .collect(Collectors.toList());
        } catch (PricingException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }

        // Cliente logada (opcional): o pedido fica no histórico dela
        Customer customer = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                customer = customerRepository.findByEmail(jwtUtil.extractEmail(token)).orElse(null);
            }
        }

        // Pedido criado aqui, com os preços do banco, para TODA compra (com ou sem conta).
        // Assim ele aparece no painel e o webhook confirma o pedido certo.
        Order order = buildOrder(req, priced, customer);
        orderRepository.save(order);

        try {
            // Build items list for MP
            List<Map<String, Object>> mpItems = priced.stream().map(item -> {
                Map<String, Object> mpItem = new LinkedHashMap<>();
                mpItem.put("id", String.valueOf(item.product().getId()));
                mpItem.put("title", item.title());
                mpItem.put("quantity", item.quantity());
                mpItem.put("unit_price", item.unitPrice());
                mpItem.put("currency_id", "BRL");
                return mpItem;
            }).collect(Collectors.toList());

            // Build payer info
            Map<String, Object> mpPayer = new LinkedHashMap<>();
            if (req.getPayer() != null) {
                mpPayer.put("name", req.getPayer().getName() != null ? req.getPayer().getName() : "");
                String email = (req.getPayer().getEmail() != null && !req.getPayer().getEmail().isBlank())
                    ? req.getPayer().getEmail()
                    : "cliente@fiosmj.com";
                mpPayer.put("email", email);
                // CPF obrigatório para boleto
                String cpf = req.getPayer().getCpf();
                if (cpf != null && !cpf.isBlank()) {
                    Map<String, String> identification = new LinkedHashMap<>();
                    identification.put("type", "CPF");
                    identification.put("number", cpf.replaceAll("[^0-9]", ""));
                    mpPayer.put("identification", identification);
                }
            } else {
                mpPayer.put("email", "cliente@fiosmj.com");
            }

            // Build back_urls
            Map<String, String> backUrls = new LinkedHashMap<>();
            String base = siteUrl.replaceAll("/+$", "");
            backUrls.put("success", base + "/obrigado");
            backUrls.put("failure", base);
            backUrls.put("pending", base + "/pendente");

            // Build request body
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("items", mpItems);
            body.put("payer", mpPayer);
            body.put("back_urls", backUrls);
            body.put("auto_return", "approved");
            body.put("notification_url", base + "/api/checkout/webhook");
            // external_reference: usado pelo webhook para identificar o pedido
            body.put("external_reference", "order-" + order.getId());

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + accessToken);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.mercadopago.com/checkout/preferences",
                HttpMethod.POST,
                entity,
                Map.class
            );

            Map<?, ?> mpBody = response.getBody();
            if (mpBody == null) {
                order.setStatus(Order.Status.CANCELLED);
                orderRepository.save(order);
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Empty response from Mercado Pago"));
            }

            String initPoint = (String) mpBody.get("init_point");
            String preferenceId = (String) mpBody.get("id");

            order.setPreferenceId(preferenceId);
            order.setInitPoint(initPoint);
            orderRepository.save(order);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("order_id", order.getId());
            result.put("init_point", initPoint);
            result.put("preference_id", preferenceId);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            order.setStatus(Order.Status.CANCELLED);
            orderRepository.save(order);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Não foi possível iniciar o pagamento. Tente de novo em instantes."));
        }
    }

    private Order buildOrder(CheckoutRequest req, List<PricedItem> priced, Customer customer) {
        Map<String, Object> data = new LinkedHashMap<>();
        if (req.getPayer() != null) {
            data.put("name", cut(req.getPayer().getName(), 120));
            data.put("email", cut(req.getPayer().getEmail(), 150));
            data.put("phone", cut(req.getPayer().getPhone(), 30));
        }
        if (req.getAddress() != null) {
            data.put("cep", cut(req.getAddress().getCep(), 12));
            data.put("street", cut(req.getAddress().getStreet(), 150));
            data.put("number", cut(req.getAddress().getNumber(), 20));
            data.put("complement", cut(req.getAddress().getComplement(), 100));
            data.put("neighborhood", cut(req.getAddress().getNeighborhood(), 100));
            data.put("city", cut(req.getAddress().getCity(), 100));
            data.put("state", cut(req.getAddress().getState(), 30));
        }
        data.put("totalAmount", Math.round(priced.stream().mapToDouble(PricedItem::subtotal).sum() * 100) / 100.0);
        data.put("items", priced.stream().map(i -> {
            Map<String, Object> im = new LinkedHashMap<>();
            im.put("productName", i.title());
            im.put("price", i.unitPrice());
            im.put("quantity", i.quantity());
            im.put("selectedSize", i.size());
            return im;
        }).collect(Collectors.toList()));
        return OrderController.buildOrderFromBody(data, customer);
    }

    private static String cut(String v, int max) {
        if (v == null) return null;
        v = v.trim();
        return v.length() > max ? v.substring(0, max) : v;
    }
}
