package com.example.demo.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.entities.OrderItem;

public interface OrderItemRepository
        extends JpaRepository<OrderItem, Integer> {

    List<OrderItem> findByOrder_OrderId(String orderId);

    @Query("""
            SELECT oi
            FROM OrderItem oi
            WHERE oi.order.userId = :userId
            AND oi.order.status = 'SUCCESS'
            """)
    List<OrderItem> findSuccessfulOrderItemsByUserId(int userId);
}