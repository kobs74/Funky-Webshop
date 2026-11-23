package com.keyscout24.routes;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.keyscout24.games.GameService;

/** Port of app/routes: /, /terms-of-service, /privacy-policy. */
@Controller
public class RoutesController {

    private final GameService gameService;

    public RoutesController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("featured_games", gameService.featured());
        model.addAttribute("discount_games", gameService.discounted());
        return "index";
    }

    @GetMapping("/terms-of-service")
    public String termsOfService() {
        return "legal/terms";
    }

    @GetMapping("/privacy-policy")
    public String privacyPolicy() {
        return "legal/privacy";
    }
}
