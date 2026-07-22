package com.user.management.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "delegations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Delegation {

    @Id
    @GeneratedValue
    UUID id;

    @Column(name = "delegator_id")
    private UUID delegatorId;

    @Column(name = "delegatee_id")
    private UUID delegateeId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> delegatedRoles;

    @NotNull(message = "Start time is required")
    @FutureOrPresent(message = "Start time must be today or in the future")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @Future(message = "End time must be in the future")
    private LocalDateTime endTime;

    private String status;

    @Column(updatable = false)
    private  LocalDateTime createdAt;

    @PrePersist
    protected void onCreate(){
        this.createdAt=LocalDateTime.now();
    }
}
