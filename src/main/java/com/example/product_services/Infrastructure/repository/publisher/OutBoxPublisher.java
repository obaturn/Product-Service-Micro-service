package com.example.product_services.Infrastructure.repository.publisher;

import com.example.product_services.Domains.model.OutboxEvent;
import com.example.product_services.Infrastructure.repository.OutBoxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutBoxPublisher {
    private static final Logger log = LoggerFactory.getLogger(OutBoxPublisher.class);
    private final OutBoxRepository outboxRepository;
    private final PulsarTemplate<String> pulsarTemplate;

    public OutBoxPublisher(OutBoxRepository outboxRepository, PulsarTemplate<String> pulsarTemplate) {
        this.outboxRepository = outboxRepository;
        this.pulsarTemplate = pulsarTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {
        var events = outboxRepository.findTop100ByPublishedFalseOrderByCreatedAtAsc();
        for (OutboxEvent event : events) {
            try {
                pulsarTemplate.send(event.getAggregateType(), event.getPayload());
                event.setPublished(true);
                event.setAttempts(event.getAttempts()+1);
                outboxRepository.save(event);
                log.info("Published event id=" + event.getId());
            } catch (Exception ex) {
                event.setAttempts(event.getAttempts()+1);
                event.setLastError(ex.getMessage());
                outboxRepository.save(event);
                log.error("Failed to publish id=" + event.getId(), ex);
                // if attempts exceed threshold, optionally publish the event payload to a DLQ topic:
                if (event.getAttempts() >= 5) {
                    try {
                        pulsarTemplate.send("product-topic-dlq", event.getPayload());
                        event.setPublished(true); // mark as handled so we don't loop forever
                        outboxRepository.save(event);
                        log.info("Moved event to DLQ id=" + event.getId());
                    } catch (Exception dlqEx) {
                        log.error("Failed to publish to DLQ for id=" + event.getId(), dlqEx);
                    }
                }
            }
        }
    }
}
