package com.fiosmj.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiosmj.app.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.*;
import java.util.*;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${openclaw.gateway.url:http://localhost:18789}")
    private String gatewayUrl;

    @Value("${openclaw.gateway.token:}")
    private String gatewayToken;

    @Value("${notification.maju.phone:+553399892409}")
    private String majuPhone;

    @Value("${notification.toledo.phone:}")
    private String toledoPhone;

    /**
     * Notifica pedido confirmado via WhatsApp para Maju e Toledo
     */
    public void notifyOrderConfirmed(Order order) {
        String msg = buildOrderConfirmedMessage(order);
        sendWhatsApp(toledoPhone, msg);
        sendWhatsApp(majuPhone, msg);
        log.info("Notificação de pedido confirmado enviada: orderId={}", order.getId());
    }

    /**
     * Notifica carrinho abandonado
     */
    public void notifyAbandonedCart(String customerName, String phone, int itemCount) {
        String msg = String.format(
            "🛒 Carrinho abandonado — Fios MJ\n\n" +
            "Cliente: %s\n" +
            "Itens no carrinho: %d\n\n" +
            "Entrar em contato para recuperar a venda? 💕",
            customerName, itemCount
        );
        sendWhatsApp(toledoPhone, msg);
        sendWhatsApp(majuPhone, msg);
    }

    private String buildOrderConfirmedMessage(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("✅ *Pedido Confirmado — Fios MJ*\n\n");
        sb.append(String.format("🆔 Pedido #%d\n", order.getId()));
        sb.append(String.format("👤 Cliente: %s\n", order.getName()));
        sb.append(String.format("📱 WhatsApp: %s\n", order.getPhone() != null ? order.getPhone() : "-"));
        sb.append(String.format("📧 E-mail: %s\n", order.getEmail() != null ? order.getEmail() : "-"));
        sb.append("\n📦 *Itens:*\n");
        if (order.getItems() != null) {
            for (var item : order.getItems()) {
                sb.append(String.format("  • %s × %d — R$%.2f\n",
                    item.getProductName(),
                    item.getQuantity(),
                    item.getPrice() * item.getQuantity()));
            }
        }
        sb.append(String.format("\n💰 *Total: R$%.2f*\n", order.getTotalAmount()));
        sb.append("\n📍 *Endereço de entrega:*\n");
        sb.append(String.format("%s, %s — %s/%s\nCEP: %s\n",
            order.getStreet() != null ? order.getStreet() + (order.getNumber() != null ? ", " + order.getNumber() : "") : "-",
            order.getNeighborhood() != null ? order.getNeighborhood() : "-",
            order.getCity() != null ? order.getCity() : "-",
            order.getState() != null ? order.getState() : "-",
            order.getCep() != null ? order.getCep() : "-"
        ));
        sb.append("\n💳 Pagamento confirmado pelo Mercado Pago ✅");
        return sb.toString();
    }

    private void sendWhatsApp(String phone, String message) {
        if (phone == null || phone.isBlank()) return;
        if (gatewayToken == null || gatewayToken.isBlank()) {
            log.debug("Notificação WhatsApp desligada (sem gateway configurado)");
            return;
        }
        try {
            Map<String, Object> args = new LinkedHashMap<>();
            args.put("channel", "whatsapp");
            args.put("to", phone);
            args.put("message", message);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("tool", "message");
            body.put("action", "send");
            body.put("args", args);
            body.put("sessionKey", "main");

            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(gatewayUrl + "/tools/invoke"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + gatewayToken)
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .build();

            HttpResponse<String> resp = HttpClient.newHttpClient()
                .send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 200) {
                log.info("WhatsApp enviado para {}", phone);
            } else {
                log.warn("Falha ao enviar WhatsApp para {} — status {}: {}", phone, resp.statusCode(), resp.body());
            }
        } catch (Exception e) {
            log.error("Erro ao enviar notificação WhatsApp para {}: {}", phone, e.getMessage());
        }
    }
}
