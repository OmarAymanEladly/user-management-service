package com.user.management.repository;

import com.user.management.entity.Delegation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DelegationRepository extends JpaRepository<Delegation, UUID> {
}
