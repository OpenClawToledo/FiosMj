package com.fiosmj.app.controller;

import com.fiosmj.app.model.Customer;
import com.fiosmj.app.model.Order;
import com.fiosmj.app.model.OrderItem;
import com.fiosmj.app.repository.OrderRepository;
import com.fiosmj.app.service.PricingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final PricingService pricing;

    public OrderController(OrderRepository orderRepository, PricingService pricing) {
        this.orderRepository = orderRepository;
        this.pricing = pricing;
    }

    @GetMapping
    public ResponseEntity<?> getOrders(@AuthenticationPrincipal Customer customer) {
        if (customer == null) return ResponseEntity.status(401).body(Map.of("error", "Não autenticado"));

        List<Order> orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId());
        List<Map<String, Object>> result = new ArrayList<>();
        for (Order o : orders) result.add(orderToMap(o));
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@AuthenticationPrincipal Customer customer, @RequestBody Map<String, Object> body) {
        if (customer == null) return ResponseEntity.status(401).body(Map.of("error", "Não autenticado"));
        try {
            repriceItems(body);
        } catch (PricingService.PricingException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
        Order order = buildOrderFromBody(body, customer);
        orderRepository.save(order);
        return ResponseEntity.ok(orderToMap(order));
    }

    /** Atualiza preferenceId e initPoint de um pedido existente */
    @PatchMapping("/{id}/preference")
    public ResponseEntity<?> updatePreference(
            @AuthenticationPrincipal Customer customer,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        if (customer == null) return ResponseEntity.status(401).body(Map.of("error", "Não autenticado"));
        return orderRepository.findById(id)
            .filter(o -> o.getCustomer().getId().equals(customer.getId()))
            .map(o -> {
                if (body.get("preferenceId") != null) o.setPreferenceId((String) body.get("preferenceId"));
                if (body.get("initPoint") != null) o.setInitPoint((String) body.get("initPoint"));
                orderRepository.save(o);
                return ResponseEntity.ok(orderToMap(o));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /** Troca preço e total enviados pelo navegador pelos valores do banco. */
    @SuppressWarnings("unchecked")
    private void repriceItems(Map<String, Object> body) {
        Object raw = body.get("items");
        if (!(raw instanceof List<?> list) || list.isEmpty() || list.size() > 50)
            throw new PricingService.PricingException("Carrinho vazio ou inválido.");
        double total = 0;
        List<Map<String, Object>> fixed = new ArrayList<>();
        for (Object o : list) {
            if (!(o instanceof Map<?, ?> m)) throw new PricingService.PricingException("Item inválido.");
            Map<String, Object> item = (Map<String, Object>) m;
            Long productId = item.get("productId") instanceof Number n ? n.longValue() : null;
            Integer qty = item.get("quantity") instanceof Number n ? n.intValue() : null;
            String size = item.get("selectedSize") != null ? item.get("selectedSize").toString() : null;
            PricingService.PricedItem p = pricing.price(productId, size, qty);
            Map<String, Object> clean = new LinkedHashMap<>();
            clean.put("productName", p.title());
            clean.put("price", p.unitPrice());
            clean.put("quantity", p.quantity());
            clean.put("selectedSize", p.size());
            fixed.add(clean);
            total += p.subtotal();
        }
        body.put("items", fixed);
        body.put("totalAmount", Math.round(total * 100) / 100.0);
    }

    public static Order buildOrderFromBody(Map<String, Object> body, Customer customer) {
        Order order = new Order();
        order.setCustomer(customer);
        order.setPreferenceId((String) body.get("preferenceId"));
        order.setInitPoint((String) body.get("initPoint"));
        order.setName((String) body.get("name"));
        order.setEmail((String) body.get("email"));
        order.setPhone((String) body.get("phone"));
        order.setCep((String) body.get("cep"));
        order.setStreet((String) body.get("street"));
        order.setNumber((String) body.get("number"));
        order.setComplement((String) body.get("complement"));
        order.setNeighborhood((String) body.get("neighborhood"));
        order.setCity((String) body.get("city"));
        order.setState((String) body.get("state"));

        if (body.get("totalAmount") != null) {
            order.setTotalAmount(Double.valueOf(body.get("totalAmount").toString()));
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemsData = (List<Map<String, Object>>) body.get("items");
        if (itemsData != null) {
            for (Map<String, Object> itemData : itemsData) {
                OrderItem item = new OrderItem();
                item.setOrder(order);
                item.setProductName((String) itemData.get("productName"));
                item.setPrice(itemData.get("price") != null ? Double.valueOf(itemData.get("price").toString()) : null);
                item.setQuantity(itemData.get("quantity") != null ? Integer.valueOf(itemData.get("quantity").toString()) : 1);
                item.setSelectedSize((String) itemData.get("selectedSize"));
                order.getItems().add(item);
            }
        }

        return order;
    }

    public static Map<String, Object> orderToMap(Order o) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", o.getId());
        m.put("status", o.getStatus());
        m.put("preferenceId", o.getPreferenceId());
        m.put("initPoint", o.getInitPoint());
        m.put("totalAmount", o.getTotalAmount());
        m.put("name", o.getName());
        m.put("email", o.getEmail());
        m.put("phone", o.getPhone());
        m.put("cep", o.getCep());
        m.put("street", o.getStreet());
        m.put("number", o.getNumber());
        m.put("complement", o.getComplement());
        m.put("neighborhood", o.getNeighborhood());
        m.put("city", o.getCity());
        m.put("state", o.getState());
        m.put("createdAt", o.getCreatedAt());

        List<Map<String, Object>> items = new ArrayList<>();
        for (OrderItem item : o.getItems()) {
            Map<String, Object> im = new LinkedHashMap<>();
            im.put("id", item.getId());
            im.put("productName", item.getProductName());
            im.put("price", item.getPrice());
            im.put("quantity", item.getQuantity());
            im.put("selectedSize", item.getSelectedSize());
            items.add(im);
        }
        m.put("items", items);
        return m;
    }
}
