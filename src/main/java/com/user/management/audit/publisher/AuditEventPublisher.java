package com.user.management.audit.publisher;

import com.user.management.audit.dto.AuditEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditEventPublisher {

    private final StreamBridge streamBridge;

    @Async
    public void publish(AuditEvent auditEvent) {
        log.info("Publishing audit event [{}] action=[{}]",
                auditEvent.getEventId(), auditEvent.getData().getActionType());
        boolean sent = streamBridge.send("audit-out-0", auditEvent);

        if (!sent) {
            log.error("Failed to publish audit event [{}] action=[{}]",
                    auditEvent.getEventId(), auditEvent.getData().getActionType());
        }

    }
}
