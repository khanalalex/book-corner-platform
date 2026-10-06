package com.bookcorner.auth.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

    /** Callers must pass an email already normalized with {@link User#normalizeEmail(String)}. */
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
