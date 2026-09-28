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
            backUrls.put("success", "https://fiosmj.com/obrigado");
            backUrls.put("failure", "https://fiosmj.com");
            backUrls.put("pending", "https://fiosmj.com/pendente");

            // Build request body
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("items", mpItems);
            body.put("payer", mpPayer);
            body.put("back_urls", backUrls);
            body.put("auto_return", "approved");
            body.put("notification_url", "https://fiosmj.com/api/checkout/webhook");
            // external_reference: usado pelo webhook para identificar o pedido
            if (req.getExternalReference() != null) {
                body.put("external_reference", req.getExternalReference());
            }

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
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Empty response from Mercado Pago"));
            }

            String initPoint = (String) mpBody.get("init_point");
            String preferenceId = (String) mpBody.get("id");

            // Save order in DB if customer is authenticated
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtUtil.validateToken(token)) {
                    String email = jwtUtil.extractEmail(token);
                    customerRepository.findByEmail(email).ifPresent(customer -> {
                        try {
                            Map<String, Object> orderData = new LinkedHashMap<>();
                            orderData.put("preferenceId", preferenceId);
                            orderData.put("initPoint", initPoint);
                            if (req.getPayer() != null) {
                                orderData.put("name", req.getPayer().getName());
                                orderData.put("email", req.getPayer().getEmail());
                                orderData.put("phone", req.getPayer().getPhone());
                            }
                            if (req.getAddress() != null) {
                                orderData.put("cep", req.getAddress().getCep());
                                orderData.put("street", req.getAddress().getStreet());
                                orderData.put("number", req.getAddress().getNumber());
                                orderData.put("complement", req.getAddress().getComplement());
                                orderData.put("neighborhood", req.getAddress().getNeighborhood());
                                orderData.put("city", req.getAddress().getCity());
                                orderData.put("state", req.getAddress().getState());
                            }
                            double total = priced.stream().mapToDouble(PricedItem::subtotal).sum();
                            orderData.put("totalAmount", total);

                            List<Map<String, Object>> itemsList = priced.stream().map(i -> {
                                Map<String, Object> im = new LinkedHashMap<>();
                                im.put("productName", i.title());
                                im.put("price", i.unitPrice());
                                im.put("quantity", i.quantity());
                                im.put("selectedSize", i.size());
                                return im;
                            }).collect(Collectors.toList());
                            orderData.put("items", itemsList);

                            Order order = OrderController.buildOrderFromBody(orderData, customer);
                            orderRepository.save(order);
                        } catch (Exception e) {
                            // Log but don't fail the checkout
                        }
                    });
                }
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("init_point", initPoint);
            result.put("preference_id", preferenceId);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Não foi possível iniciar o pagamento. Tente de novo em instantes."));
        }
    }
}
