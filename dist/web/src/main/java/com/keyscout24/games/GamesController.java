package com.keyscout24.games;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.keyscout24.auth.CurrentUser;
import com.keyscout24.auth.UserPrincipal;
import com.keyscout24.db.CartItemRepository;
import com.keyscout24.db.Game;
import com.keyscout24.util.Flash;
import com.keyscout24.util.Gift;

/** Port of app/games. */
@Controller
public class GamesController {

    private final GameService gameService;
    private final CartItemRepository cartItems;
    private final Gift gift;

    public GamesController(GameService gameService, CartItemRepository cartItems, Gift gift) {
        this.gameService = gameService;
        this.cartItems = cartItems;
        this.gift = gift;
    }

    @GetMapping("/game/list")
    public String gameList(@RequestParam(defaultValue = "1") int page, Model model) {
        GameService.GamePage result = gameService.listAll(page);
        model.addAttribute("games", result.games());
        model.addAttribute("page", page);
        model.addAttribute("total_pages", result.totalPages());
        return "games/list";
    }

    @GetMapping("/game/my-games")
    public String myGames(@RequestParam(defaultValue = "1") int page, Model model) {
        UserPrincipal user = CurrentUser.require();
        GameService.GamePage result = gameService.listBySeller(user.id(), page);
        model.addAttribute("games", result.games());
        model.addAttribute("page", page);
        model.addAttribute("total_pages", result.totalPages());
        model.addAttribute("total_games", result.totalGames());
        model.addAttribute("total_value", gameService.totalValue(user.id()));
        model.addAttribute("discount_count", gameService.discountCount(user.id()));
        return "games/mygames";
    }

    @GetMapping("/game/{id}")
    public String gameDetail(@PathVariable int id, Model model) {
        Game game = gameService.findEntity(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        boolean isOwnGame = false;
        boolean inCart = false;
        var user = CurrentUser.get();
        if (user.isPresent()) {
            isOwnGame = game.getSeller().getId().equals(user.get().id());
            inCart = cartItems.existsByUser_IdAndGame_Id(user.get().id(), game.getId());
        }

        model.addAttribute("game", gameService.find(id).orElseThrow());
        model.addAttribute("is_own_game", isOwnGame);
        model.addAttribute("in_cart", inCart);
        return "games/detail";
    }

    @GetMapping("/game/add")
    @PreAuthorize("hasAuthority('game:add')")
    public String gameAddPage(Model model) {
        model.addAttribute("form", Map.of());
        return "games/add";
    }

    @PostMapping("/game/add")
    @PreAuthorize("hasAuthority('game:add')")
    public String gameAdd(@RequestParam Map<String, String> params, Model model, RedirectAttributes redirect) {
        String name = param(params, "name");
        String description = param(params, "description");
        String platform = param(params, "platform");
        String store = param(params, "store");
        String key = param(params, "key");
        String price = param(params, "price");
        boolean discount = "on".equals(params.get("discount"));

        List<String> errors = GameService.validate(name, description, platform, store, key, price);
        if (!errors.isEmpty()) {
            errors.forEach(error -> Flash.error(model, error));
            model.addAttribute("form", new HashMap<>(params));
            return "games/add";
        }

        Integer id = gameService.add(new GameService.NewGame(name, description.isEmpty() ? null : description,
                platform, store, key, Double.parseDouble(price), discount), CurrentUser.require().id());

        Flash.success(redirect, "Game added successfully!");
        return "redirect:/game/" + id;
    }

    @PostMapping("/game/{id}/gift-code")
    public String createGiftCode(@PathVariable int id, @RequestParam(defaultValue = "") String message,
                                 RedirectAttributes redirect) {
        var game = gameService.findEntity(id);
        if (game.isEmpty()) {
            Flash.error(redirect, "Game not found");
            return "redirect:/game/my-games";
        }
        if (!game.get().getSeller().getId().equals(CurrentUser.require().id())) {
            Flash.error(redirect, "You can only create gift codes for your own games");
            return "redirect:/game/my-games";
        }

        message = message.strip();
        if (message.codePointCount(0, message.length()) > 500) {
            Flash.error(redirect, "Message must be 500 characters or less.");
            return "redirect:/game/my-games";
        }

        // Intentionally unsanitized, like the Flask version (flag store 1, vuln 2).
        String giftCodeData = "{\"id\": " + game.get().getId() + ", \"message\": \"" + message + "\"}";
        String giftCode = gift.pack(gift.create(giftCodeData));

        Flash.success(redirect, "Gift code for \"" + game.get().getName() + "\": " + giftCode);
        return "redirect:/game/my-games";
    }

    @GetMapping("/game/{id}/key")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getGameKey(@PathVariable int id) {
        var game = gameService.findEntity(id);
        if (game.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Game not found"));
        }
        if (!game.get().getSeller().getId().equals(CurrentUser.require().id())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You can only view keys for your own games"));
        }
        Map<String, String> body = new LinkedHashMap<>();
        body.put("key", game.get().getKey());
        body.put("name", game.get().getName());
        return ResponseEntity.ok(body);
    }

    private static String param(Map<String, String> params, String name) {
        String value = params.get(name);
        return value == null ? "" : value.strip();
    }
}
