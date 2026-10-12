package com.bookcorner.auth.registration;

import com.bookcorner.auth.user.Role;
import com.bookcorner.auth.user.RoleName;
import com.bookcorner.auth.user.RoleRepository;
import com.bookcorner.auth.user.User;
import com.bookcorner.auth.user.UserRepository;
import com.bookcorner.auth.user.UserResponse;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {

    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(UserRepository users, RoleRepository roles, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Returns a DTO (not the entity) and builds it inside the transaction, so lazy collections are safe to read
     * and the entity never leaks out of the service layer.
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = User.normalizeEmail(request.email());

        // Fast, friendly check for the common case...
        if (users.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException();
        }

        Role userRole = roles.findByName(RoleName.USER)
                .orElseThrow(() -> new IllegalStateException("Role USER is missing: were the migrations applied?"));

        User user = new User(email, passwordEncoder.encode(request.password()), request.fullName().trim());
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }
        user.addRole(userRole);

        try {
            return UserResponse.from(users.saveAndFlush(user));
        } catch (DataIntegrityViolationException e) {
            // ...but only the database's unique constraint is race-proof: two simultaneous requests can both
            // pass the check above. The loser lands here.
            throw new EmailAlreadyRegisteredException();
        }
    }
}
