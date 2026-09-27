package com.fiosmj.app.repository;

import com.fiosmj.app.model.SavedCart;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SavedCartRepository extends JpaRepository<SavedCart, Long> {
    Optional<SavedCart> findByCustomerId(Long customerId);
}
