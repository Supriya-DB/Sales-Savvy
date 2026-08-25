package com.example.demo.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.demo.entities.OrderItem;
import com.example.demo.entities.Product;
import com.example.demo.entities.ProductImage;
import com.example.demo.entities.User;
import com.example.demo.repositories.OrderItemRepository;
import com.example.demo.repositories.ProductImageRepository;
import com.example.demo.repositories.ProductRepository;

@Service
public class OrderService {

    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    public OrderService(
            OrderItemRepository orderItemRepository,
            ProductRepository productRepository,
            ProductImageRepository productImageRepository) {

        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
    }

    public Map<String, Object> getOrdersForUser(User user) {

        Map<String, Object> response = new HashMap<>();

        response.put("role", user.getRole().name());
        response.put("username", user.getUsername());

        List<OrderItem> orderItems =
                orderItemRepository
                        .findSuccessfulOrderItemsByUserId(
                                user.getUserId()
                        );

        List<Map<String, Object>> products =
                new ArrayList<>();

        for (OrderItem item : orderItems) {

            Product product =
                    productRepository
                            .findById(item.getProductId())
                            .orElse(null);

            if (product == null) {
                continue;
            }

            List<ProductImage> images =
                    productImageRepository
                            .findByProduct_ProductId(
                                    item.getProductId()
                            );

            String imageUrl =
                    images.isEmpty()
                            ? null
                            : images.get(0).getImageUrl();

            Map<String, Object> productDetails =
                    new HashMap<>();

            productDetails.put(
                    "order_id",
                    item.getOrder().getOrderId()
            );

            productDetails.put(
                    "status",
                    item.getOrder().getStatus().name()
            );

            productDetails.put(
                    "order_date",
                    item.getOrder().getCreatedAt()
            );

            productDetails.put(
                    "order_total",
                    item.getOrder().getTotalAmount()
            );

            productDetails.put(
                    "quantity",
                    item.getQuantity()
            );

            productDetails.put(
                    "total_price",
                    item.getTotalPrice()
            );

            productDetails.put(
                    "price_per_unit",
                    item.getPricePerUnit()
            );

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
                    "image_url",
                    imageUrl
            );

            products.add(productDetails);
        }

        response.put("products", products);

        return response;
    }
}