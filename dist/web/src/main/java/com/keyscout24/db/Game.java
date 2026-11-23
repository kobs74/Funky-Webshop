package com.keyscout24.db;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Port of db/Game.py. Column names must stay as they are: the govapi C binary reads
 * id, name, price, seller_id, description and key straight from this table.
 */
@Entity
@Table(name = "game")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 40)
    private String name;

    @Column(nullable = false, length = 20)
    private String platform;

    @Column(nullable = false, length = 20)
    private String store;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Boolean discount = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(length = 255)
    private String description;

    @Column(name = "key", nullable = false, length = 40)
    private String key;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Game() {
    }

    public Game(String name, String description, String platform, String store, String key,
                Double price, Boolean discount, User seller) {
        this.name = name;
        this.description = description;
        this.platform = platform;
        this.store = store;
        this.key = key;
        this.price = price;
        this.discount = discount;
        this.seller = seller;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getPlatform() { return platform; }
    public String getStore() { return store; }
    public Double getPrice() { return price; }
    public Boolean getDiscount() { return discount; }
    public User getSeller() { return seller; }
    public String getDescription() { return description; }
    public String getKey() { return key; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
