package com.keyscout24.db;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    long countByUser_Id(Integer userId);

    boolean existsByUser_IdAndGame_Id(Integer userId, Integer gameId);
}
