package com.bookcorner.auth.user;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** What the API shows about a user. Deliberately has no password hash and no internal fields. */
public record UserResponse(
        UUID id,
        String email,
        String fullName,
        String phone,
        boolean emailVerified,
        List<String> roles,
        Instant createdAt) {

    public static UserResponse from(User user) {
        List<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().name())
                .sorted()
                .toList();
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.isEmailVerified(),
                roleNames,
                user.getCreatedAt());
    }
}
