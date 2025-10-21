package com.example.product_services.Infrastructure.repository;

import com.example.product_services.Domains.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutBoxRepository extends JpaRepository<OutboxEvent, Long> {
}
