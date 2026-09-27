package com.fiosmj.app.controller;

import com.fiosmj.app.model.Customer;
import com.fiosmj.app.model.SavedCart;
import com.fiosmj.app.model.SavedCartItem;
import com.fiosmj.app.repository.SavedCartRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final SavedCartRepository savedCartRepository;

    public CartController(SavedCartRepository savedCartRepository) {
        this.savedCartRepository = savedCartRepository;
    }

    @GetMapping
    public ResponseEntity<?> getCart(@AuthenticationPrincipal Customer customer) {
        if (customer == null) return ResponseEntity.status(401).body(Map.of("error", "Não autenticado"));

        return savedCartRepository.findByCustomerId(customer.getId())
            .map(cart -> ResponseEntity.ok(cartToMap(cart)))
            .orElse(ResponseEntity.ok(Map.of("items", List.of())));
    }

    @PostMapping
    public ResponseEntity<?> saveCart(@AuthenticationPrincipal Customer customer, @RequestBody List<Map<String, Object>> itemsBody) {
        if (customer == null) return ResponseEntity.status(401).body(Map.of("error", "Não autenticado"));

        SavedCart cart = savedCartRepository.findByCustomerId(customer.getId())
            .orElse(new SavedCart());
        cart.setCustomer(customer);
        cart.getItems().clear();

        for (Map<String, Object> itemData : itemsBody) {
            SavedCartItem item = new SavedCartItem();
            item.setCart(cart);
            item.setProductId(itemData.get("productId") != null ? Long.valueOf(itemData.get("productId").toString()) : null);
            item.setProductName((String) itemData.get("productName"));
            item.setPrice(itemData.get("price") != null ? Double.valueOf(itemData.get("price").toString()) : null);
            item.setQuantity(itemData.get("quantity") != null ? Integer.valueOf(itemData.get("quantity").toString()) : 1);
            item.setSelectedSize((String) itemData.get("selectedSize"));
            cart.getItems().add(item);
        }

        savedCartRepository.save(cart);
        return ResponseEntity.ok(cartToMap(cart));
    }

    @DeleteMapping
    public ResponseEntity<?> clearCart(@AuthenticationPrincipal Customer customer) {
        if (customer == null) return ResponseEntity.status(401).body(Map.of("error", "Não autenticado"));

        savedCartRepository.findByCustomerId(customer.getId()).ifPresent(cart -> {
            cart.getItems().clear();
            savedCartRepository.save(cart);
        });
        return ResponseEntity.ok(Map.of("message", "Carrinho limpo"));
    }

    private Map<String, Object> cartToMap(SavedCart cart) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> itemsList = new ArrayList<>();
        for (SavedCartItem item : cart.getItems()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", item.getId());
            m.put("productId", item.getProductId());
            m.put("productName", item.getProductName());
            m.put("price", item.getPrice());
            m.put("quantity", item.getQuantity());
            m.put("selectedSize", item.getSelectedSize());
            itemsList.add(m);
        }
        result.put("items", itemsList);
        result.put("updatedAt", cart.getUpdatedAt());
        return result;
    }
}
