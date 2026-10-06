package com.bookcorner.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import com.bookcorner.auth.token.RefreshToken;
import com.bookcorner.auth.token.RefreshTokenRepository;
import com.bookcorner.auth.user.Role;
import com.bookcorner.auth.user.RoleName;
import com.bookcorner.auth.user.RoleRepository;
import com.bookcorner.auth.user.User;
import com.bookcorner.auth.user.UserRepository;
import com.bookcorner.auth.user.UserStatus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Runs the real Flyway migrations against a real MySQL started by Testcontainers (Docker must be running),
 * then lets Hibernate validate that the entities match the schema. Each test rolls back its data.
 */
@SpringBootTest(properties = {
        // "tc" = Testcontainers JDBC URL: starts a throwaway MySQL 8.4 container for the test run
        "spring.datasource.url=jdbc:tc:mysql:8.4:///auth_test",
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
        "spring.datasource.username=test",
        "spring.datasource.password=test",
        "eureka.client.enabled=false",
        "spring.cloud.config.enabled=false"
})
@Transactional
class AuthPersistenceTest {

    @Autowired
    private UserRepository users;

    @Autowired
    private RoleRepository roles;

    @Autowired
    private RefreshTokenRepository refreshTokens;

    @Test
    void migrationSeedsExactlyTheFourRoles() {
        assertThat(roles.findAll()).extracting(Role::getName)
                .containsExactlyInAnyOrder(RoleName.USER, RoleName.SELLER, RoleName.ADMIN, RoleName.RIDER);
    }

    @Test
    void savesAUserWithARoleAndFindsItByEmail() {
        User user = new User("reader@example.com", "{bcrypt}hash", "Reader One");
        user.addRole(roles.findByName(RoleName.USER).orElseThrow());
        users.saveAndFlush(user);

        User found = users.findByEmail("reader@example.com").orElseThrow();

        assertThat(found.getId()).isNotNull();
        assertThat(found.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(found.isEmailVerified()).isFalse();
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getRoles()).extracting(Role::getName).containsExactly(RoleName.USER);
    }

    @Test
    void emailIsTrimmedAndLowercased() {
        User user = new User("  Mixed.Case@Example.COM ", "{bcrypt}hash", "Mixed Case");

        assertThat(user.getEmail()).isEqualTo("mixed.case@example.com");
    }

    @Test
    void theDatabaseRejectsADuplicateEmail() {
        users.saveAndFlush(new User("same@example.com", "{bcrypt}hash", "First"));

        assertThatThrownBy(() -> users.saveAndFlush(new User("same@example.com", "{bcrypt}hash", "Second")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void savesAndFindsARefreshTokenByItsHash() {
        User user = users.saveAndFlush(new User("token@example.com", "{bcrypt}hash", "Token User"));
        Instant now = Instant.now();
        String hash = "a".repeat(64);
        refreshTokens.saveAndFlush(new RefreshToken(
                user.getId(), UUID.randomUUID(), hash, now, now.plus(14, ChronoUnit.DAYS)));

        RefreshToken found = refreshTokens.findByTokenHash(hash).orElseThrow();

        assertThat(found.getUserId()).isEqualTo(user.getId());
        assertThat(found.isRevoked()).isFalse();
        assertThat(found.isExpired(now)).isFalse();
    }
}
