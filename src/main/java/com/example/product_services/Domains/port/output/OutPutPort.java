package com.example.product_services.Domains.port.output;

public interface OutPutPort {
    void saveEvent(Object payloadObject, String aggregateType, String eventType);
}
