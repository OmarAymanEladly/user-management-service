package com.user.management.repository;

import com.user.management.model.entity.UserTypeRule;
import com.user.management.model.enumeration.EventSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserTypeRuleRepository extends JpaRepository<UserTypeRule, UUID> {

    @Query("""
        SELECT r FROM UserTypeRule r
        JOIN FETCH r.userType
        WHERE r.active = true
          AND (r.applicableSource IS NULL OR r.applicableSource = :source)
        ORDER BY r.priority DESC
        """)
    List<UserTypeRule> findApplicableRules(@Param("source") EventSource source);
}