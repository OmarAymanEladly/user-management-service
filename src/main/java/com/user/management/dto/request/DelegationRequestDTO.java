package com.user.management.dto.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DelegationRequestDTO {
    private UUID delegatorId;
    private UUID delegateeId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
