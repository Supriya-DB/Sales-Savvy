package com.example.demo.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entities.Order;

public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findByUserId(int userId);

}