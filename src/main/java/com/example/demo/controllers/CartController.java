package com.example.demo.controllers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entities.CartItem;
import com.example.demo.entities.User;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.CartService;
import com.example.demo.services.ProductService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(
        origins = "http://localhost:5173",
        allowCredentials = "true"
)
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductService productService;


    // =========================
    // ADD TO CART
    // POST /api/cart/add
    // =========================
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {

        User user = (User) httpRequest.getAttribute(
                "authenticatedUser"
        );

        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
        }

        Integer productId =
                ((Number) request.get("productId"))
                        .intValue();

        Integer quantity =
                request.get("quantity") != null
                        ? ((Number) request.get("quantity"))
                                .intValue()
                        : 1;

        cartService.addToCart(
                user.getUsername(),
                productId,
                quantity
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Product added to cart successfully"
                )
        );
    }


    // =========================
    // VIEW CART ITEMS
    // GET /api/cart
    // (uses the authenticated user from the session cookie,
    // not a client-supplied username)
    // =========================
    @GetMapping
    public ResponseEntity<?> getCartItems(
            HttpServletRequest httpRequest) {

        User user = (User) httpRequest.getAttribute(
                "authenticatedUser"
        );

        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
        }

        List<CartItem> cartItems =
                cartService.getCartItems(user.getUsername());

        List<Map<String, Object>> response = new ArrayList<>();

        for (CartItem item : cartItems) {

            Map<String, Object> itemMap = new HashMap<>();

            itemMap.put("id", item.getId());
            itemMap.put("quantity", item.getQuantity());

            Map<String, Object> productMap = new HashMap<>();

            productMap.put(
                    "productId",
                    item.getProduct().getProductId()
            );

            productMap.put(
                    "name",
                    item.getProduct().getName()
            );

            productMap.put(
                    "description",
                    item.getProduct().getDescription()
            );

            productMap.put(
                    "price",
                    item.getProduct().getPrice()
            );

            productMap.put(
                    "stock",
                    item.getProduct().getStock()
            );

            // Pull real images from the productimages table,
            // same as /api/products does
            List<String> images = productService.getProductImages(
                    item.getProduct().getProductId()
            );

            productMap.put("images", images);

            itemMap.put("product", productMap);

            response.add(itemMap);
        }

        return ResponseEntity.ok(response);
    }


    // =========================
    // UPDATE QUANTITY
    // PUT /api/cart/update/1
    // =========================
    @PutMapping("/update/{cartItemId}")
    public ResponseEntity<?> updateCartItem(
            @PathVariable Integer cartItemId,
            @RequestBody Map<String, Object> request) {

        Integer quantity =
                ((Number) request.get("quantity"))
                        .intValue();

        CartItem updatedItem =
                cartService.updateCartItem(
                        cartItemId,
                        quantity
                );

        return ResponseEntity.ok(updatedItem);
    }


    // =========================
    // DELETE CART ITEM
    // DELETE /api/cart/delete/1
    // =========================
    @DeleteMapping("/delete/{cartItemId}")
    public ResponseEntity<?> deleteCartItem(
            @PathVariable Integer cartItemId) {

        cartService.deleteCartItem(cartItemId);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Cart item deleted successfully"
                )
        );
    }


    // =========================
    // GET TOTAL CART COUNT
    // GET /api/cart/items/count
    // =========================
    @GetMapping("/items/count")
    public ResponseEntity<?> getCartCount(
            HttpServletRequest httpRequest) {

        User user = (User) httpRequest.getAttribute(
                "authenticatedUser"
        );

        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
        }

        int count = cartService.getCartItemCount(
                user.getUserId()
        );

        return ResponseEntity.ok(count);
    }


    // =========================
    // GET TOTAL PRICE
    // GET /api/cart/total
    // =========================
    @GetMapping("/total")
    public ResponseEntity<?> getCartTotal(
            HttpServletRequest httpRequest) {

        User user = (User) httpRequest.getAttribute(
                "authenticatedUser"
        );

        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
        }

    	BigDecimal total = cartService.getCartTotal(user.getUsername());

        return ResponseEntity.ok(
                Map.of("total", total)
        );
    }


    // =========================
    // CLEAR COMPLETE CART
    // DELETE /api/cart/clear
    // =========================
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(
            HttpServletRequest httpRequest) {

        User user = (User) httpRequest.getAttribute(
                "authenticatedUser"
        );

        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
        }

        cartService.clearCart(user.getUsername());

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Cart cleared successfully"
                )
        );
    }
}