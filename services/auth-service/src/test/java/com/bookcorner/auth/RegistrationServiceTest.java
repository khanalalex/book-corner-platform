package com.bookcorner.auth;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.bookcorner.auth.registration.EmailAlreadyRegisteredException;
import com.bookcorner.auth.registration.RegisterRequest;
import com.bookcorner.auth.registration.RegistrationService;
import com.bookcorner.auth.user.Role;
import com.bookcorner.auth.user.RoleName;
import com.bookcorner.auth.user.RoleRepository;
import com.bookcorner.auth.user.User;
import com.bookcorner.auth.user.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Fast unit tests, no Spring and no database: they cover the branches that are hard to reach over HTTP. */
class RegistrationServiceTest {

    private final UserRepository users = mock(UserRepository.class);
    private final RoleRepository roles = mock(RoleRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final RegistrationService service = new RegistrationService(users, roles, encoder);

    private final RegisterRequest request =
            new RegisterRequest("race@example.com", "a-long-enough-password", "Race Condition", null);

    @Test
    void doesNotSaveWhenTheEmailIsAlreadyKnown() {
        when(users.existsByEmail("race@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(request)).isInstanceOf(EmailAlreadyRegisteredException.class);

        verify(users, never()).saveAndFlush(any());
    }

    @Test
    void treatsADatabaseUniqueViolationAsAnAlreadyRegisteredEmail() {
        // Two simultaneous requests both pass the existsByEmail check; the database rejects the second insert.
        when(users.existsByEmail("race@example.com")).thenReturn(false);
        when(roles.findByName(RoleName.USER)).thenReturn(Optional.of(mock(Role.class)));
        when(encoder.encode(any(CharSequence.class))).thenReturn("{bcrypt}hash");
        when(users.saveAndFlush(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> service.register(request)).isInstanceOf(EmailAlreadyRegisteredException.class);
    }

    @Test
    void failsLoudlyWhenTheUserRoleIsMissing() {
        when(users.existsByEmail("race@example.com")).thenReturn(false);
        when(roles.findByName(RoleName.USER)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("migrations");
    }
}
