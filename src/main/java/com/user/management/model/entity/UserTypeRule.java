package com.user.management.model.entity;


import com.user.management.model.enumeration.EventSource;
<<<<<<< HEAD
import com.user.management.model.event.RuleEvaluatorType;
=======
import com.user.management.model.enumeration.RuleEvaluatorType;
>>>>>>> 9082cad52326dd5df1a5ed89e2b8aec65a0d54c2
import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Entity
@Table(name = "user_type_rules")
@Getter
public class UserTypeRule {

    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    private RuleEvaluatorType evaluatorType;

    @Column(nullable = false)
    private String matchKey;

    @Column(nullable = false)
    private String matchValue;

    @Enumerated(EnumType.STRING)
    private EventSource applicableSource;

    @Column(nullable = false)
    private int priority;

    @Column(nullable = false)
    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_type_id", nullable = false)
    private UserType userType;
}
