package com.fiosmj.app.repository;

import com.fiosmj.app.model.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
    List<ContactMessage> findTop200ByOrderByCreatedAtDesc();
}
