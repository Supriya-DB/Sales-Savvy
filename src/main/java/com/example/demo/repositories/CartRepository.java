package com.example.demo.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entities.CartItem;
import com.example.demo.entities.Product;
import com.example.demo.entities.User;

@Repository
public interface CartRepository extends JpaRepository<CartItem, Integer> {

    // Find all cart items for a user
    List<CartItem> findByUser(User user);

    // Find existing cart item for same user and product
    Optional<CartItem> findByUserAndProduct(
            User user,
            Product product
    );

    // Get total quantity of items in cart
    @Query("""
            SELECT COALESCE(SUM(c.quantity), 0)
            FROM CartItem c
            WHERE c.user.userId = :userId
            """)
    int getTotalCount(
            @Param("userId") Integer userId
    );

    // Get cart items with product details
    @Query("""
            SELECT c
            FROM CartItem c
            JOIN FETCH c.product
            WHERE c.user.userId = :userId
            """)
    List<CartItem> findCartItemsWithProductDetails(
            @Param("userId") Integer userId
    );

    // Delete all cart items of a user
    @Modifying
    @Transactional
    @Query("""
            DELETE FROM CartItem c
            WHERE c.user.userId = :userId
            """)
    void deleteAllCartItemsByUserId(
            @Param("userId") Integer userId
    );
}