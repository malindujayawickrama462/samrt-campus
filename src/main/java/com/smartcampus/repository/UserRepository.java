package com.smartcampus.repository;

import com.smartcampus.entity.User;
import com.smartcampus.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
    List<User> findByRole(Role role);
    boolean existsByEmail(String email);
}
