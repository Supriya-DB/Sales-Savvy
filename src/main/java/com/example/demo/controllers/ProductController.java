package com.example.demo.controllers;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entities.*;
import com.example.demo.services.ProductService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getProducts(
            @RequestParam(required = false) String category,
            HttpServletRequest request) {

        try {

            // Retrieve authenticated user from the request attribute set by the filter
            User authenticatedUser =
                    (User) request.getAttribute("authenticatedUser");

            if (authenticatedUser == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Unauthorized access"));
            }

            // Fetch products based on the category filter
            List<Product> products =
                    productService.getProductsByCategory(category);

            // Build the response
            Map<String, Object> response = new HashMap<>();

            // Add user info
            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("name", authenticatedUser.getUsername());
            userInfo.put("role", authenticatedUser.getRole().name());

            response.put("user", userInfo);

            // Add product details
            List<Map<String, Object>> productList = new ArrayList<>();

            for (Product product : products) {

                Map<String, Object> productDetails = new HashMap<>();

                productDetails.put("product_id", product.getProductId());
                productDetails.put("name", product.getName());
                productDetails.put("description", product.getDescription());
                productDetails.put("price", product.getPrice());
                productDetails.put("stock", product.getStock());

                // Fetch product images
                List<String> images =
                        productService.getProductImages(product.getProductId());

                productDetails.put("images", images);

                productList.add(productDetails);
            }

            response.put("products", productList);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}