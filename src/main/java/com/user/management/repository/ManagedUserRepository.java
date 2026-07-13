package com.user.management.repository;

import com.user.management.entity.ManagedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ManagedUserRepository extends JpaRepository<ManagedUser, UUID> {
    Optional<ManagedUser> findByUsername(String username);
    List<ManagedUser> findBySyncStatus(String syncStatus);

}
