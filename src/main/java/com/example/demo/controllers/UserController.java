package com.example.demo.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entities.User;
import com.example.demo.services.UserService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


    // =====================================================
    // REGISTER USER
    // =====================================================

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @RequestBody User user) {

        try {

            User registeredUser =
                    userService.registerUser(user);

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "User registered successfully",

                            "user",
                            registeredUser
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }


    // =====================================================
    // GET LOGGED-IN USER PROFILE
    // =====================================================

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(
            HttpServletRequest request) {

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


        return ResponseEntity.ok(
                Map.of(
                        "username",
                        authenticatedUser.getUsername(),

                        "email",
                        authenticatedUser.getEmail(),

                        "role",
                        authenticatedUser
                                .getRole()
                                .name()
                )
        );
    }
}