package com.fiosmj.app.controller;

import com.fiosmj.app.model.Order;
import com.fiosmj.app.repository.OrderRepository;
import com.fiosmj.app.service.NotificationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/checkout")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    @Autowired private OrderRepository orderRepo;
    @Autowired private NotificationService notificationService;

    @Value("${mercadopago.access-token}")
    private String accessToken;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Webhook do Mercado Pago — chamado quando há atualização de pagamento.
     *
     * MP envia:
     *   POST /api/checkout/webhook?topic=payment&id=<payment_id>
     * ou body:
     *   { "action": "payment.updated", "data": { "id": "<payment_id>" } }
     */
    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String id,
            @RequestBody(required = false) String rawBody) {

        log.info("Webhook MP recebido — topic={} id={} body={}", topic, id,
            rawBody != null ? rawBody.substring(0, Math.min(200, rawBody.length())) : "");

        String paymentId = id;

        // Tentar extrair payment_id do body se não vier na query
        if ((paymentId == null || paymentId.isBlank()) && rawBody != null && !rawBody.isBlank()) {
            try {
                JsonNode node = mapper.readTree(rawBody);
                // Formato: {"action":"payment.updated","data":{"id":"123"}}
                if (node.has("data") && node.get("data").has("id")) {
                    paymentId = node.get("data").get("id").asText();
                    topic = "payment";
                }
                // Formato alternativo: {"type":"payment","data":{"id":"123"}}
                if (paymentId == null && node.has("type") && node.has("data")) {
                    paymentId = node.get("data").get("id").asText();
                    topic = node.get("type").asText();
                }
            } catch (Exception e) {
                log.warn("Erro ao parsear body do webhook: {}", e.getMessage());
            }
        }

        if (!"payment".equals(topic) || paymentId == null || paymentId.isBlank()) {
            // Outros tipos (merchant_order, etc.) — apenas ACK
            return ResponseEntity.ok(Map.of("received", true));
        }

        // Buscar detalhes do pagamento na API do Mercado Pago
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.mercadopago.com/v1/payments/" + paymentId,
                HttpMethod.GET, entity, Map.class
            );

            Map<?, ?> payment = response.getBody();
            if (payment == null) {
                log.warn("Resposta vazia da API MP para paymentId={}", paymentId);
                return ResponseEntity.ok(Map.of("received", true));
            }

            String status = (String) payment.get("status");
            String prefId = (String) payment.get("preference_id");
            String extRef = (String) payment.get("external_reference");

            log.info("Pagamento {} — status={} prefId={} extRef={}", paymentId, status, prefId, extRef);

            if ("approved".equals(status)) {
                // Tentar por preference_id primeiro, depois por external_reference
                Optional<Order> orderOpt = Optional.empty();
                if (prefId != null && !prefId.isEmpty()) {
                    orderOpt = orderRepo.findByPreferenceId(prefId);
                }
                if (orderOpt.isEmpty() && extRef != null && !extRef.isEmpty()) {
                    try {
                        Long orderId = Long.parseLong(extRef.replace("order-", ""));
                        orderOpt = orderRepo.findById(orderId);
                    } catch (NumberFormatException ignored) {}
                }
                orderOpt.ifPresent(order -> {
                    if (order.getStatus() != Order.Status.CONFIRMED) {
                        order.setStatus(Order.Status.CONFIRMED);
                        orderRepo.save(order);
                        log.info("Pedido #{} confirmado via webhook", order.getId());
                        notificationService.notifyOrderConfirmed(order);
                    }
                });
            }

        } catch (Exception e) {
            log.error("Erro ao processar webhook MP paymentId={}: {}", paymentId, e.getMessage());
        }

        return ResponseEntity.ok(Map.of("received", true));
    }
}
