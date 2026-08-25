package com.example.demo.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entities.Product;
import com.example.demo.entities.User;
import com.example.demo.services.ProductService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin(
    origins = "http://localhost:5173",
    allowCredentials = "true"
)
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;


    // =========================================================
    // GET ALL PRODUCTS
    // GET /api/products
    // =========================================================

    @GetMapping
    public ResponseEntity<Map<String, Object>> getProducts(
            @RequestParam(required = false) String category,
            HttpServletRequest request) {

        try {

            List<Product> products =
                    productService.getProductsByCategory(category);

            return buildProductResponse(products, request);

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                        "error",
                        e.getMessage()
                    ));
        }
    }


    // =========================================================
    // GET PRODUCTS BY CATEGORY ID
    // GET /api/products/category/{categoryId}
    // =========================================================

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Map<String, Object>> getProductsByCategory(
            @PathVariable Integer categoryId,
            HttpServletRequest request) {

        try {

            List<Product> products =
                    productService.getProductsByCategoryId(categoryId);

            return buildProductResponse(products, request);

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                        "error",
                        e.getMessage()
                    ));
        }
    }


    // =========================================================
    // BUILD PRODUCT RESPONSE
    // =========================================================

    private ResponseEntity<Map<String, Object>> buildProductResponse(
            List<Product> products,
            HttpServletRequest request) {

        Map<String, Object> response =
                new HashMap<>();


        // -----------------------------------------------------
        // User information
        // -----------------------------------------------------

        User authenticatedUser =
                (User) request.getAttribute("authenticatedUser");

        if (authenticatedUser != null) {

            Map<String, String> userInfo =
                    new HashMap<>();

            userInfo.put(
                "name",
                authenticatedUser.getUsername()
            );

            userInfo.put(
                "role",
                authenticatedUser.getRole().name()
            );

            response.put("user", userInfo);
        }


        // -----------------------------------------------------
        // Product details
        // -----------------------------------------------------

        List<Map<String, Object>> productList =
                new ArrayList<>();


        for (Product product : products) {

            Map<String, Object> productDetails =
                    new HashMap<>();

            productDetails.put(
                "product_id",
                product.getProductId()
            );

            productDetails.put(
                "name",
                product.getName()
            );

            productDetails.put(
                "description",
                product.getDescription()
            );

            productDetails.put(
                "price",
                product.getPrice()
            );

            productDetails.put(
                "stock",
                product.getStock()
            );


            // Product images

            List<String> images =
                    productService.getProductImages(
                        product.getProductId()
                    );

            productDetails.put(
                "images",
                images
            );


            productList.add(productDetails);
        }


        response.put(
            "products",
            productList
        );


        return ResponseEntity.ok(response);
    }
}