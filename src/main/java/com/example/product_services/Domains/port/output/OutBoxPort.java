package com.example.product_services.Domains.port.output;

public interface OutBoxPort {
    void saveEvent(Object payloadObject, String aggregateType, String eventType);
}
