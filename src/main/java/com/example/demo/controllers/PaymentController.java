package com.example.demo.controllers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entities.User;
import com.example.demo.services.PaymentService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(
        origins = "http://localhost:5173",
        allowCredentials = "true"
)
public class PaymentController {

    @Autowired
    private PaymentService paymentService;


    // ==========================================
    // CREATE RAZORPAY ORDER
    // POST /api/payment/create
    // ==========================================
    @PostMapping("/create")
    public ResponseEntity<?> createPaymentOrder(
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest request) {

        try {

            // ==========================================
            // GET AUTHENTICATED USER
            // ==========================================
            User user = (User) request.getAttribute(
                    "authenticatedUser"
            );

            if (user == null) {

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(
                                Map.of(
                                        "error",
                                        "User not authenticated"
                                )
                        );
            }


            // ==========================================
            // VALIDATE TOTAL AMOUNT
            // ==========================================
            if (requestBody.get("totalAmount") == null) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "error",
                                        "totalAmount is required"
                                )
                        );
            }


            // ==========================================
            // CONVERT AMOUNT TO BIGDECIMAL
            // ==========================================
            BigDecimal totalAmount =
                    new BigDecimal(
                            requestBody
                                    .get("totalAmount")
                                    .toString()
                    );


            // ==========================================
            // CHECK VALID AMOUNT
            // ==========================================
            if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "error",
                                        "Total amount must be greater than zero"
                                )
                        );
            }


            // ==========================================
            // CREATE RAZORPAY ORDER
            // ==========================================
            String razorpayOrderId =
                    paymentService.createOrder(

                            // FIX:
                            // userId was undefined
                            // Use authenticated user's ID
                            user.getUserId(),

                            totalAmount
                    );


            // ==========================================
            // RETURN RESPONSE
            // ==========================================
            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Razorpay order created successfully",

                            "razorpayOrderId",
                            razorpayOrderId,

                            "totalAmount",
                            totalAmount,

                            "currency",
                            "INR"
                    )
            );


        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            Map.of(
                                    "error",
                                    "Error creating payment order: "
                                            + e.getMessage()
                            )
                    );
        }
    }


    // ==========================================
    // VERIFY RAZORPAY PAYMENT
    // POST /api/payment/verify
    // ==========================================
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest request) {

        try {

            // ==========================================
            // GET AUTHENTICATED USER
            // ==========================================
            User user = (User) request.getAttribute(
                    "authenticatedUser"
            );

            if (user == null) {

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(
                                Map.of(
                                        "error",
                                        "User not authenticated"
                                )
                        );
            }


            // ==========================================
            // VALIDATE RAZORPAY RESPONSE
            // ==========================================
            if (requestBody.get("razorpayOrderId") == null ||
                    requestBody.get("razorpayPaymentId") == null ||
                    requestBody.get("razorpaySignature") == null) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "error",
                                        "razorpayOrderId, razorpayPaymentId and razorpaySignature are required"
                                )
                        );
            }


            // ==========================================
            // GET PAYMENT VALUES
            // ==========================================
            String razorpayOrderId =
                    requestBody
                            .get("razorpayOrderId")
                            .toString();

            String razorpayPaymentId =
                    requestBody
                            .get("razorpayPaymentId")
                            .toString();

            String razorpaySignature =
                    requestBody
                            .get("razorpaySignature")
                            .toString();


            // ==========================================
            // VERIFY PAYMENT
            // ==========================================
            boolean isVerified =
                    paymentService.verifyPayment(

                            razorpayOrderId,

                            razorpayPaymentId,

                            razorpaySignature,

                            user.getUserId()
                    );


            // ==========================================
            // PAYMENT SUCCESS
            // ==========================================
            if (isVerified) {

                return ResponseEntity.ok(
                        Map.of(
                                "message",
                                "Payment verified successfully",

                                "paymentStatus",
                                "SUCCESS"
                        )
                );
            }


            // ==========================================
            // PAYMENT FAILED
            // ==========================================
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            Map.of(
                                    "error",
                                    "Payment verification failed",

                                    "paymentStatus",
                                    "FAILED"
                            )
                    );


        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "error",
                                    "Error verifying payment: "
                                            + e.getMessage()
                            )
                    );
        }
    }
}