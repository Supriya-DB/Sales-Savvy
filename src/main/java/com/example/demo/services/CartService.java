package com.example.demo.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entities.CartItem;
import com.example.demo.entities.Product;
import com.example.demo.entities.User;
import com.example.demo.repositories.CartRepository;
import com.example.demo.repositories.ProductRepository;
import com.example.demo.repositories.UserRepository;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;


    // =========================
    // ADD PRODUCT TO CART
    // =========================
    public void addToCart(
            String username,
            Integer productId,
            Integer quantity) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        Optional<CartItem> existingItem =
                cartRepository.findByUserAndProduct(user, product);

        if (existingItem.isPresent()) {

            CartItem cartItem = existingItem.get();

            cartItem.setQuantity(
                    cartItem.getQuantity() + quantity
            );

            cartRepository.save(cartItem);

        } else {

            CartItem newItem =
                    new CartItem(user, product, quantity);

            cartRepository.save(newItem);
        }
    }


    // =========================
    // VIEW ALL CART ITEMS
    // =========================
    public List<CartItem> getCartItems(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return cartRepository.findByUser(user);
    }


    // =========================
    // UPDATE CART ITEM QUANTITY
    // =========================
    public CartItem updateCartItem(
            Integer cartItemId,
            Integer quantity) {

        if (quantity == null || quantity <= 0) {
            throw new RuntimeException(
                    "Quantity must be greater than 0"
            );
        }

        CartItem cartItem = cartRepository.findById(cartItemId)
                .orElseThrow(() ->
                        new RuntimeException("Cart item not found"));

        cartItem.setQuantity(quantity);

        return cartRepository.save(cartItem);
    }


    // =========================
    // DELETE CART ITEM
    // =========================
    public void deleteCartItem(Integer cartItemId) {

        CartItem cartItem = cartRepository.findById(cartItemId)
                .orElseThrow(() ->
                        new RuntimeException("Cart item not found"));

        cartRepository.delete(cartItem);
    }


    // =========================
    // GET TOTAL CART QUANTITY
    // =========================
    public int getCartItemCount(Integer userId) {

        return cartRepository.getTotalCount(userId);
    }


    // =========================
    // GET CART TOTAL PRICE
    // =========================
    public BigDecimal getCartTotal(String username) {

        List<CartItem> cartItems = getCartItems(username);

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem item : cartItems) {

            BigDecimal itemTotal = item.getProduct()
                    .getPrice()
                    .multiply(
                            BigDecimal.valueOf(item.getQuantity())
                    );

            total = total.add(itemTotal);
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }


    // =========================
    // CLEAR COMPLETE CART
    // =========================
    public void clearCart(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        List<CartItem> cartItems =
                cartRepository.findByUser(user);

        cartRepository.deleteAll(cartItems);
    }
}