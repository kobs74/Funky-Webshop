package com.keyscout24.auth;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.keyscout24.db.User;
import com.keyscout24.db.UserRepository;

/** Port of create_user / check_user_exists and the password check from auth/__init__.py. */
@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    /** Returns false if the username is already taken. */
    @Transactional
    public boolean createUser(String username, String password, String role) {
        if (users.existsByUsername(username)) {
            return false;
        }
        try {
            users.saveAndFlush(new User(username, passwordEncoder.encode(password), role));
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }

    public boolean checkUserExists(String username) {
        return users.existsByUsername(username);
    }

    /** Checks the password and updates last_login. Empty if the username or password is wrong. */
    @Transactional
    public Optional<UserPrincipal> login(String username, String password) {
        return users.findByUsername(username)
                .filter(user -> passwordEncoder.matches(password, user.getPasswordHash()))
                .map(user -> {
                    user.setLastLogin(LocalDateTime.now());
                    return UserPrincipal.of(user);
                });
    }
}
