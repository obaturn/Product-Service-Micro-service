package com.example.product_services.Infrastructure.repository.publisher;

import com.example.product_services.Domains.model.OutboxEvent;
import com.example.product_services.Infrastructure.repository.OutBoxRepository;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class OutBoxPublisher {
    private final OutBoxRepository outboxRepository;
    private final PulsarTemplate<String> pulsarTemplate;
    public OutBoxPublisher(OutBoxRepository outboxRepository, PulsarTemplate<String> pulsarTemplate) {
        this.outboxRepository = outboxRepository;
        this.pulsarTemplate = pulsarTemplate;
    }
    @Scheduled(fixedDelay = 10000)
    public void publishPendingEvents() {
        List<OutboxEvent> events = outboxRepository.findAll()
                .stream()
                .filter(e -> !e.isPublished())
                .toList();

        for (OutboxEvent event : events) {
            try {
                // 🔹 Publish the event payload to Pulsar
                pulsarTemplate.send(event.getAggregateType(), event.getPayload());

                // 🔹 Mark as published
                event.setPublished(true);
                outboxRepository.save(event);

                System.out.println(" Published event: " + event.getEventType());
            } catch (Exception ex) {
                System.err.println(" Failed to publish event: " + ex.getMessage());
            }
        }
    }
}
