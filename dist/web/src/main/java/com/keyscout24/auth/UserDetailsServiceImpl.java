package com.keyscout24.auth;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.keyscout24.db.UserRepository;

/**
 * Loads users from PostgreSQL for Spring Security. Login itself is handled in AuthController
 * (to keep the Flask behaviour), but this bean replaces Spring's default in-memory user.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository users;

    public UserDetailsServiceImpl(UserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return users.findByUsername(username)
                .map(user -> org.springframework.security.core.userdetails.User
                        .withUsername(user.getUsername())
                        .password(user.getPasswordHash())
                        .authorities(UserPrincipal.of(user).authorities())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }
}
