package com.fiosmj.app.scheduler;

import com.fiosmj.app.model.Order;
import com.fiosmj.app.model.SavedCart;
import com.fiosmj.app.repository.OrderRepository;
import com.fiosmj.app.repository.SavedCartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AbandonedCartScheduler {

    private static final Logger log = LoggerFactory.getLogger(AbandonedCartScheduler.class);

    private final SavedCartRepository savedCartRepository;
    private final OrderRepository orderRepository;

    public AbandonedCartScheduler(SavedCartRepository savedCartRepository, OrderRepository orderRepository) {
        this.savedCartRepository = savedCartRepository;
        this.orderRepository = orderRepository;
    }

    @Scheduled(cron = "0 0 10 * * ?")
    public void checkAbandonedCarts() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);

        List<SavedCart> carts = savedCartRepository.findAll();
        for (SavedCart cart : carts) {
            if (cart.getUpdatedAt() == null || cart.getItems().isEmpty()) continue;
            if (cart.getUpdatedAt().isAfter(cutoff)) continue; // updated within last 24h

            // Check if customer placed an order in last 24h
            List<Order> recentOrders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(cart.getCustomer().getId());
            boolean hasRecentOrder = recentOrders.stream()
                .anyMatch(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(cutoff));

            if (!hasRecentOrder) {
                log.info("Carrinho abandonado: customer={}", cart.getCustomer().getEmail());
            }
        }
    }
}
