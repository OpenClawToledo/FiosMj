package com.fiosmj.app.repository;

import com.fiosmj.app.model.InstagramPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InstagramPostRepository extends JpaRepository<InstagramPost, Long> {
    List<InstagramPost> findByActiveTrueOrderByDisplayOrderAsc();
}
