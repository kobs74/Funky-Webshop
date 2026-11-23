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

/** Port of db/UserGameKey.py: a key a user received by redeeming a gift code. */
@Entity
@Table(name = "user_game_key")
public class UserGameKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 40)
    private String name;

    @Column(nullable = false, length = 20)
    private String platform;

    @Column(nullable = false, length = 20)
    private String store;

    @Column(name = "key", nullable = false, length = 40)
    private String key;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected UserGameKey() {
    }

    public UserGameKey(User user, String name, String platform, String store, String key) {
        this.user = user;
        this.name = name;
        this.platform = platform;
        this.store = store;
        this.key = key;
    }

    public Integer getId() { return id; }
    public User getUser() { return user; }
    public String getName() { return name; }
    public String getPlatform() { return platform; }
    public String getStore() { return store; }
    public String getKey() { return key; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
