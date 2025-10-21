package com.example.product_services.Adapter.persistenceProduct;

import com.example.product_services.Domains.model.OutboxEvent;

import com.example.product_services.Domains.port.output.OutBoxPort;
import com.example.product_services.Infrastructure.repository.OutBoxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class OutboxAdapter implements OutBoxPort {

    private final OutBoxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OutboxAdapter(OutBoxRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void saveEvent(Object payloadObject, String aggregateType, String eventType) {
        try {
            OutboxEvent event = new OutboxEvent();
            event.setAggregateType(aggregateType);
            event.setEventType(eventType);
            event.setPayload(objectMapper.writeValueAsString(payloadObject));
            event.setPublished(false);
            outboxRepository.save(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write outbox event", e);
        }
    }
}
