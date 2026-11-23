package com.keyscout24.games;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.keyscout24.db.Game;
import com.keyscout24.db.GameRepository;
import com.keyscout24.db.User;
import com.keyscout24.db.UserRepository;

/** Game queries and the validation of the "add game" form (games/__init__.py). */
@Service
public class GameService {

    public static final int PER_PAGE = 20;

    private final GameRepository games;
    private final UserRepository users;

    public GameService(GameRepository games, UserRepository users) {
        this.games = games;
        this.users = users;
    }

    public record GamePage(List<GameDto> games, int page, int totalPages, long totalGames) {
    }

    public record NewGame(String name, String description, String platform, String store, String key,
                          double price, boolean discount) {
    }

    @Transactional(readOnly = true)
    public GamePage listAll(int page) {
        Page<Game> result = games.findAllByOrderByCreatedAtDesc(PageRequest.of(Math.max(page, 1) - 1, PER_PAGE));
        return toPage(result, page);
    }

    @Transactional(readOnly = true)
    public GamePage listBySeller(Integer sellerId, int page) {
        Page<Game> result = games.findBySeller_IdOrderByCreatedAtDesc(sellerId,
                PageRequest.of(Math.max(page, 1) - 1, PER_PAGE));
        return toPage(result, page);
    }

    public double totalValue(Integer sellerId) {
        return games.sumPriceBySellerId(sellerId);
    }

    public long discountCount(Integer sellerId) {
        return games.countBySeller_IdAndDiscountTrue(sellerId);
    }

    @Transactional(readOnly = true)
    public List<GameDto> featured() {
        return games.findTop4ByOrderByCreatedAtDesc().stream().map(GameDto::of).toList();
    }

    @Transactional(readOnly = true)
    public List<GameDto> discounted() {
        return games.findTop3ByDiscountTrueOrderByCreatedAtDesc().stream().map(GameDto::of).toList();
    }

    @Transactional(readOnly = true)
    public Optional<GameDto> find(Integer id) {
        return games.findById(id).map(GameDto::of);
    }

    /** The full entity incl. key and seller id. Only for owner checks and the key endpoint. */
    @Transactional(readOnly = true)
    public Optional<Game> findEntity(Integer id) {
        return games.findById(id).map(game -> {
            game.getSeller().getId();
            return game;
        });
    }

    @Transactional
    public Integer add(NewGame form, Integer sellerId) {
        User seller = users.getReferenceById(sellerId);
        Game game = new Game(form.name(), form.description(), form.platform(), form.store(), form.key(),
                form.price(), form.discount(), seller);
        return games.save(game).getId();
    }

    /** Same checks and messages as game_add() in Flask. Returns the errors; empty means valid. */
    public static List<String> validate(String name, String description, String platform, String store,
                                        String key, String price) {
        List<String> errors = new ArrayList<>();

        if (name.isEmpty()) {
            errors.add("Game name is required.");
        } else if (length(name) > 40) {
            errors.add("Game name must be 40 characters or less.");
        }

        if (!description.isEmpty() && length(description) > 255) {
            errors.add("Description must be 255 characters or less.");
        }

        if (platform.isEmpty()) {
            errors.add("Platform is required.");
        } else if (length(platform) > 20) {
            errors.add("Platform must be 20 characters or less.");
        }

        if (store.isEmpty()) {
            errors.add("Store is required.");
        } else if (length(store) > 20) {
            errors.add("Store must be 20 characters or less.");
        }

        if (key.isEmpty()) {
            errors.add("Game key is required.");
        } else if (length(key) > 40) {
            errors.add("Game key must be 40 characters or less.");
        }

        if (price.isEmpty()) {
            errors.add("Price is required.");
        } else {
            try {
                double value = Double.parseDouble(price);
                if (value < 0) {
                    errors.add("Price must be a positive number.");
                } else if (value > 10000) {
                    errors.add("Price must not exceed $10,000.");
                }
            } catch (NumberFormatException e) {
                errors.add("Price must be a valid number.");
            }
        }
        return errors;
    }

    /** Python len(): counts characters (code points), not UTF-16 units. */
    private static int length(String value) {
        return value.codePointCount(0, value.length());
    }

    private static GamePage toPage(Page<Game> result, int page) {
        List<GameDto> dtos = result.getContent().stream().map(GameDto::of).toList();
        return new GamePage(dtos, page, result.getTotalPages(), result.getTotalElements());
    }
}
