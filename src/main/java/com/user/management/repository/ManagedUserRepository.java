package com.user.management.repository;

import com.user.management.entity.ManagedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ManagedUserRepository extends JpaRepository<ManagedUser, Long> {
    Optional<ManagedUser> findByUsername(String username);
}
