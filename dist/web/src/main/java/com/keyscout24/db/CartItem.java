package com.keyscout24.db;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** Port of db/CartItem.py. */
@Entity
@Table(name = "cart_item",
       uniqueConstraints = @UniqueConstraint(name = "unique_user_game_cart", columnNames = {"user_id", "game_id"}))
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected CartItem() {
    }

    public CartItem(User user, Game game) {
        this.user = user;
        this.game = game;
    }

    public Integer getId() { return id; }
    public User getUser() { return user; }
    public Game getGame() { return game; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
