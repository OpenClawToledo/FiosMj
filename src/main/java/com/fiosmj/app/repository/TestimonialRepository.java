package com.fiosmj.app.repository;

import com.fiosmj.app.model.Testimonial;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TestimonialRepository extends JpaRepository<Testimonial, Long> {
    List<Testimonial> findByApprovedTrueOrderByCreatedAtDesc();
    List<Testimonial> findByApprovedTrueAndOccasionIsNotNullOrderByCreatedAtDesc();
    List<Testimonial> findByApprovedTrueAndFeaturedTrueOrderByCreatedAtDesc();
    List<Testimonial> findByApprovedFalseOrderByCreatedAtAsc();
    List<Testimonial> findByApprovedTrueAndProductIdOrderByCreatedAtDesc(Long productId);
}
