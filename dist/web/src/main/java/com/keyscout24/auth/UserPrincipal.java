package com.keyscout24.auth;

import java.io.Serializable;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.keyscout24.db.User;

/**
 * The logged-in user kept in the session (Flask: session['user_id'] + g.user).
 * Its permissions become Spring Security authorities, so @PreAuthorize("hasAuthority('game:add')") works.
 */
public record UserPrincipal(Integer id, String username, String role) implements Serializable {

    public static UserPrincipal of(User user) {
        return new UserPrincipal(user.getId(), user.getUsername(), user.getRole());
    }

    public boolean hasPermission(String permission) {
        return Permissions.has(role, permission);
    }

    public Collection<? extends GrantedAuthority> authorities() {
        return Permissions.forRole(role).stream().map(SimpleGrantedAuthority::new).toList();
    }

    /** Lets templates use ${currentUser.username}. */
    public String getUsername() {
        return username;
    }

    public Integer getId() {
        return id;
    }
}
