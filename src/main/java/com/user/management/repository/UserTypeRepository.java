package com.user.management.repository;

import com.user.management.entity.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserTypeRepository extends JpaRepository<UserType, UUID> {


    Optional<UserType> findByType(String name);
}
