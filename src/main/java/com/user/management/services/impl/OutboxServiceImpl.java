package com.user.management.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.user.management.entity.OutboxEvent;
import com.user.management.repository.OutboxEventRepository;
import com.user.management.services.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxServiceImpl implements OutboxService {

    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void saveEvent(UUID aggregateId, String aggregateType, String eventType, Object payloadObj, String status) {
        try {
            String payload = payloadObj != null ? objectMapper.writeValueAsString(payloadObj) : "{}";
            OutboxEvent event = OutboxEvent.builder()
                    .aggregateId(aggregateId)
                    .aggregateType(aggregateType)
                    .eventType(eventType)
                    .payload(payload)
                    .createdAt(LocalDateTime.now())
                    .status(status)
                    .retryCount(0)
                    .build();
            outboxRepository.save(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize outbox payload", e);
        }
    }
}