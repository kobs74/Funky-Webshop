package com.keyscout24.games;

import java.time.LocalDateTime;

import com.keyscout24.db.Game;

/** Port of sanitize_game(): everything about a game except its key. */
public record GameDto(Integer id, String name, String description, String platform, String store,
                      Double price, Boolean discount, LocalDateTime createdAt, LocalDateTime updatedAt,
                      String sellerUsername) {

    public static GameDto of(Game game) {
        return new GameDto(game.getId(), game.getName(), game.getDescription(), game.getPlatform(),
                game.getStore(), game.getPrice(), game.getDiscount(), game.getCreatedAt(), game.getUpdatedAt(),
                game.getSeller().getUsername());
    }
}
