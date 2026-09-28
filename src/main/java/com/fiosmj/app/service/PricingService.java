package com.fiosmj.app.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiosmj.app.model.Product;
import com.fiosmj.app.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Preço oficial dos itens do carrinho, sempre lido do banco.
 * O navegador manda só produto, tamanho e quantidade; o valor enviado por ele é ignorado.
 */
@Service
public class PricingService {

    public static final int MAX_QTY = 20;

    private final ProductRepository products;
    private final ObjectMapper objectMapper;

    public PricingService(ProductRepository products, ObjectMapper objectMapper) {
        this.products = products;
        this.objectMapper = objectMapper;
    }

    /** Item com preço conferido. */
    public record PricedItem(Product product, String title, String size, double unitPrice, int quantity) {
        public double subtotal() { return unitPrice * quantity; }
    }

    /** Erro de validação com mensagem que pode ser mostrada à cliente. */
    public static class PricingException extends RuntimeException {
        public PricingException(String message) { super(message); }
    }

    public PricedItem price(Long productId, String selectedSize, Integer quantity) {
        if (productId == null) throw new PricingException("Item sem produto. Atualize a página e tente de novo.");

        Product p = products.findById(productId)
                .filter(Product::isActive)
                .orElseThrow(() -> new PricingException("Um produto do carrinho não está mais disponível."));

        String availability = p.getAvailability();
        if ("INDISPONIVEL".equals(availability))
            throw new PricingException("\"" + p.getName() + "\" está indisponível no momento.");
        if ("SOB_CONSULTA".equals(availability))
            throw new PricingException("\"" + p.getName() + "\" tem preço sob consulta. Fale conosco pelo WhatsApp.");
        if (p.getStock() != null && p.getStock() <= 0)
            throw new PricingException("\"" + p.getName() + "\" está esgotado.");

        int qty = quantity == null ? 1 : quantity;
        if (qty < 1 || qty > MAX_QTY)
            throw new PricingException("Quantidade inválida para \"" + p.getName() + "\".");
        if (p.getStock() != null && qty > p.getStock())
            throw new PricingException("Só temos " + p.getStock() + " unidade(s) de \"" + p.getName() + "\".");

        List<Map<String, Object>> sizes = sizesOf(p);
        double unit;
        String size = null;
        if (!sizes.isEmpty()) {
            if (selectedSize == null || selectedSize.isBlank())
                throw new PricingException("Escolha o tamanho de \"" + p.getName() + "\".");
            Map<String, Object> match = sizes.stream()
                    .filter(s -> selectedSize.equals(String.valueOf(s.get("size"))))
                    .findFirst()
                    .orElseThrow(() -> new PricingException("Tamanho inválido para \"" + p.getName() + "\"."));
            unit = toDouble(match.get("price"));
            size = selectedSize;
        } else {
            unit = p.getPrice() == null ? 0 : p.getPrice();
        }
        if (unit <= 0)
            throw new PricingException("\"" + p.getName() + "\" está sem preço. Fale conosco pelo WhatsApp.");

        unit = Math.round(unit * 100) / 100.0;
        String title = p.getName() + (size != null ? " (" + size + ")" : "");
        return new PricedItem(p, title, size, unit, qty);
    }

    private List<Map<String, Object>> sizesOf(Product p) {
        if (p.getSizesJson() == null || p.getSizesJson().isBlank()) return List.of();
        try {
            return objectMapper.readValue(p.getSizesJson(), new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private static double toDouble(Object v) {
        if (v instanceof Number n) return n.doubleValue();
        try { return v == null ? 0 : Double.parseDouble(v.toString()); } catch (NumberFormatException e) { return 0; }
    }
}
