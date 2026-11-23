package com.keyscout24.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.SessionFlashMapManager;
import org.springframework.web.util.UriComponentsBuilder;

import com.keyscout24.util.Flash;
import com.keyscout24.util.FlashMessage;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Replaces login_required / permission_required from auth/__init__.py.
 * - URLs below need a logged-in user, otherwise: flash + redirect to /login?next=...
 * - Permissions are checked with @PreAuthorize("hasAuthority('...')") on controller methods.
 * - CSRF is off and the session cookie is called "session", like the Flask app.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository contextRepository)
            throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .requestCache(cache -> cache.disable())
            .securityContext(context -> context.securityContextRepository(contextRepository))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/logout", "/game/my-games", "/game/add", "/game/*/gift-code", "/game/*/key",
                        "/cart", "/cart/**", "/profile", "/profile/**", "/support", "/support/**")
                    .authenticated()
                .anyRequest().permitAll())
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(loginRedirect())
                .accessDeniedHandler((request, response, denied) -> response.sendError(403)));
        return http.build();
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /** Flask: flash('You need to be logged in...') + redirect(url_for('auth.login', next=request.url)). */
    private static AuthenticationEntryPoint loginRedirect() {
        return (request, response, authException) -> {
            FlashMap flash = new FlashMap();
            List<FlashMessage> messages = new ArrayList<>();
            messages.add(new FlashMessage("error", "You need to be logged in to access this page."));
            flash.put(Flash.ATTRIBUTE, messages);
            new SessionFlashMapManager().saveOutputFlashMap(flash, request, response);

            String next = request.getRequestURL()
                    + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
            String target = UriComponentsBuilder.fromPath("/login").queryParam("next", next)
                    .encode().build().toUriString();
            redirect(response, target);
        };
    }

    private static void redirect(HttpServletResponse response, String target) throws IOException {
        response.sendRedirect(target);
    }
}
