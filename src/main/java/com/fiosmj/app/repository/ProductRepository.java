package com.fiosmj.app.repository;

import com.fiosmj.app.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByActiveTrueOrderByDisplayOrderAscIdAsc();
    List<Product> findAllByOrderByDisplayOrderAscIdAsc();
    List<Product> findByCategoryAndActiveTrue(String category);
}
