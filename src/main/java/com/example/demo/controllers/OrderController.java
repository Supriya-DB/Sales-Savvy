package com.example.demo.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entities.User;
import com.example.demo.services.OrderService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin(
        origins = "http://localhost:5173",
        allowCredentials = "true"
)
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getOrdersForUser(
            HttpServletRequest request) {

        try {

            User authenticatedUser =
                    (User) request.getAttribute(
                            "authenticatedUser"
                    );

            if (authenticatedUser == null) {

                return ResponseEntity
                        .status(401)
                        .body(
                                Map.of(
                                        "error",
                                        "User not authenticated"
                                )
                        );
            }

            Map<String, Object> response =
                    orderService.getOrdersForUser(
                            authenticatedUser
                    );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(500)
                    .body(
                            Map.of(
                                    "error",
                                    "An Unexpected Error Occurred"
                            )
                    );
        }
    }
}