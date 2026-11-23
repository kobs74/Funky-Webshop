package com.keyscout24.db;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GameRepository extends JpaRepository<Game, Integer> {

    @EntityGraph(attributePaths = "seller")
    Page<Game> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = "seller")
    Page<Game> findBySeller_IdOrderByCreatedAtDesc(Integer sellerId, Pageable pageable);

    @EntityGraph(attributePaths = "seller")
    List<Game> findTop4ByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "seller")
    List<Game> findTop3ByDiscountTrueOrderByCreatedAtDesc();

    long countBySeller_Id(Integer sellerId);

    long countBySeller_IdAndDiscountTrue(Integer sellerId);

    @Query("select coalesce(sum(g.price), 0) from Game g where g.seller.id = :sellerId")
    double sumPriceBySellerId(@Param("sellerId") Integer sellerId);
}
