package com.keyscout24.auth;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.keyscout24.util.Flash;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/** Port of app/auth: /login, /register, /logout. */
@Controller
public class AuthController {

    private final AuthService authService;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(AuthService authService, SecurityContextRepository securityContextRepository) {
        this.authService = authService;
        this.securityContextRepository = securityContextRepository;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password,
                        HttpServletRequest request, HttpServletResponse response, Model model) {
        var principal = authService.login(username.toLowerCase(), password);
        if (principal.isEmpty()) {
            Flash.error(model, "Incorrect username or password.");
            return "auth/login";
        }

        // session.clear(): start a fresh session for the logged-in user
        HttpSession old = request.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        request.getSession(true);

        UserPrincipal user = principal.get();
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(user, null, user.authorities()));
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        return "redirect:/";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String password,
                           Model model, RedirectAttributes redirect) {
        username = username.toLowerCase();
        int usernameLength = username.codePointCount(0, username.length());
        int passwordLength = password.codePointCount(0, password.length());
        String error = null;

        if (username.isEmpty()) {
            error = "Username is required.";
        } else if (usernameLength < 5 || usernameLength > 40) {
            error = "Username must be between 5 and 40 characters.";
        } else if (password.isEmpty()) {
            error = "Password is required.";
        } else if (passwordLength < 8 || passwordLength > 20) {
            error = "Password must be between 8 and 20 characters.";
        }

        if (error == null) {
            if (authService.createUser(username, password, "user")) {
                Flash.success(redirect, "User was successfully created! You can now login!");
                return "redirect:/login";
            }
            error = "User already exists.";
        }
        Flash.error(model, error);
        return "auth/register";
    }

    /** Only reachable when logged in (see SecurityConfig). */
    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }
}
