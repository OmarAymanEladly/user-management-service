package com.user.management.repository;

import com.user.management.model.entity.ManagedUser;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ManagedUserRepository extends JpaRepository<ManagedUser, UUID> {
    Optional<ManagedUser> findByUsername(String username);
    List<ManagedUser> findBySignupApprovalStatusIgnoreCase(String signupApprovalStatus);
    @Modifying
    @Transactional
    @Query("DELETE FROM ManagedUser u WHERE u.id = :id")
    void deleteByIdIfExists(UUID id);
}
