package com.keyscout24.auth;

import java.util.Map;
import java.util.Set;

/** Port of User.has_permission(): which role may do what. */
public final class Permissions {

    private static final Map<String, Set<String>> BY_ROLE = Map.of(
            "user", Set.of("game:add", "game:delete", "support:createTicket", "support:closeTicket",
                    "support:sendMessage", "support:readChat"),
            "bot", Set.of("support:createTicket", "support:closeTicket", "support:sendMessage",
                    "support:readChat", "support:admin"),
            "admin", Set.of("game:add", "game:delete", "user:manage", "support:admin", "admin:manage"),
            "supervisor", Set.of("supervisor:view"));

    private Permissions() {
    }

    public static Set<String> forRole(String role) {
        return BY_ROLE.getOrDefault(role, Set.of());
    }

    public static boolean has(String role, String permission) {
        return forRole(role).contains(permission);
    }
}
