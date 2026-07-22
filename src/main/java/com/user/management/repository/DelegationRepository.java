package com.user.management.repository;

import com.user.management.entity.Delegation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface DelegationRepository extends JpaRepository<Delegation, UUID> {

    List<Delegation> findByStatusAndStartTimeBefore(String status, LocalDateTime time);

    List<Delegation> findByStatusAndEndTimeBefore(String status, LocalDateTime time);

}
