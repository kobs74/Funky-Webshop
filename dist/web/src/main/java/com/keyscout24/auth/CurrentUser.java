package com.keyscout24.auth;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Static access to the logged-in user, like Flask's g.user. */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static Optional<UserPrincipal> get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    /** For endpoints that Spring Security already restricts to logged-in users. */
    public static UserPrincipal require() {
        return get().orElseThrow(() -> new IllegalStateException("No logged-in user"));
    }
}
