package com.example.demo.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entities.CartItem;
import com.example.demo.entities.Order;
import com.example.demo.entities.OrderItem;
import com.example.demo.entities.OrderStatus;
import com.example.demo.repositories.CartRepository;
import com.example.demo.repositories.OrderItemRepository;
import com.example.demo.repositories.OrderRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

@Service
public class PaymentService {

    @Value("${razorpay.key_id}")
    private String razorpayKeyId;

    @Value("${razorpay.key_secret}")
    private String razorpayKeySecret;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;

    public PaymentService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            CartRepository cartRepository) {

        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
    }


    // ==========================================
    // CREATE RAZORPAY ORDER
    // ==========================================
    @Transactional
    public String createOrder(
            int userId,
            BigDecimal totalAmount)
            throws RazorpayException {

        RazorpayClient razorpayClient =
                new RazorpayClient(
                        razorpayKeyId,
                        razorpayKeySecret
                );

        JSONObject orderRequest = new JSONObject();

        long amountInPaise = totalAmount
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, java.math.RoundingMode.HALF_UP)
                .longValueExact();

        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put(
                "receipt",
                "txn_" + System.currentTimeMillis()
        );

        com.razorpay.Order razorpayOrder =
                razorpayClient.orders.create(orderRequest);

        String razorpayOrderId =
                razorpayOrder.get("id");

        Order order = new Order();

        order.setOrderId(razorpayOrderId);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        orderRepository.save(order);

        return razorpayOrderId;
    }

    // ==========================================
    // VERIFY PAYMENT
    // ==========================================

    @Transactional
    public boolean verifyPayment(
            String razorpayOrderId,
            String razorpayPaymentId,
            String razorpaySignature,
            int userId) {

        try {

            // Prepare signature verification data
            JSONObject attributes = new JSONObject();

            attributes.put(
                    "razorpay_order_id",
                    razorpayOrderId
            );

            attributes.put(
                    "razorpay_payment_id",
                    razorpayPaymentId
            );

            attributes.put(
                    "razorpay_signature",
                    razorpaySignature
            );

            // Verify signature
            boolean isSignatureValid =
                    com.razorpay.Utils.verifyPaymentSignature(
                            attributes,
                            razorpayKeySecret
                    );

            if (!isSignatureValid) {
                return false;
            }


            // Find local order
            Order order = orderRepository
                    .findById(razorpayOrderId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Order not found"
                            )
                    );


            // Verify order belongs to logged-in user
            if (order.getUserId() != userId) {

                throw new RuntimeException(
                        "Unauthorized order access"
                );
            }


            // Prevent duplicate processing
            if (order.getStatus() == OrderStatus.SUCCESS) {
                return true;
            }


            // ==========================================
            // GET USER CART ITEMS
            // ==========================================

            List<CartItem> cartItems =
                    cartRepository
                            .findCartItemsWithProductDetails(
                                    userId
                            );

            if (cartItems.isEmpty()) {

                throw new RuntimeException(
                        "Cart is empty"
                );
            }


            // ==========================================
            // SAVE ORDER ITEMS
            // ==========================================

            for (CartItem cartItem : cartItems) {

                OrderItem orderItem =
                        new OrderItem();

                orderItem.setOrder(order);

                orderItem.setProductId(
                        cartItem.getProduct()
                                .getProductId()
                );

                orderItem.setQuantity(
                        cartItem.getQuantity()
                );

                BigDecimal pricePerUnit =
                        cartItem.getProduct()
                                .getPrice();

                orderItem.setPricePerUnit(
                        pricePerUnit
                );

                orderItem.setTotalPrice(
                        pricePerUnit.multiply(
                                BigDecimal.valueOf(
                                        cartItem.getQuantity()
                                )
                        )
                );

                orderItemRepository.save(
                        orderItem
                );
            }


            // ==========================================
            // UPDATE ORDER STATUS
            // ==========================================

            order.setStatus(
                    OrderStatus.SUCCESS
            );

            order.setUpdatedAt(
                    LocalDateTime.now()
            );

            orderRepository.save(order);


            // ==========================================
            // CLEAR USER CART
            // ==========================================

            cartRepository
                    .deleteAllCartItemsByUserId(
                            userId
                    );

            return true;

        } catch (Exception e) {

            e.printStackTrace();

            return false;
        }
    }


    // ==========================================
    // SAVE ORDER ITEMS MANUALLY
    // ==========================================

    @Transactional
    public void saveOrderItems(
            String orderId,
            List<OrderItem> items) {

        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order not found"
                        )
                );

        for (OrderItem item : items) {

            item.setOrder(order);

            orderItemRepository.save(item);
        }
    }
}