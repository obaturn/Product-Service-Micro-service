package com.example.product_services.Infrastructure.repository;

import com.example.product_services.Domains.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutBoxRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findTop100ByPublishedFalseOrderByCreatedAtAsc();

}
