package com.keyscout24.db;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGameKeyRepository extends JpaRepository<UserGameKey, Integer> {

    List<UserGameKey> findByUser_IdOrderByCreatedAtDesc(Integer userId);
}
