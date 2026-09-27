package com.fiosmj.app.repository;

import com.fiosmj.app.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<Order> findByStatusAndCreatedAtBefore(Order.Status status, LocalDateTime dateTime);
    Optional<Order> findByPreferenceId(String preferenceId);
}
