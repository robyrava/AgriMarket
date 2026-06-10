package com.agrimarket.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailSimulatorService {

    public void sendOrderConfirmedEmail(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Invalid order ID for confirmation");
        }
        log.info("--- NOTIFICATION ---");
        log.info("To: customer@agrimarket.com");
        log.info("Subject: Order Confirmed!");
        log.info("Body: Your order with ID {} has been successfully confirmed.", orderId);
        log.info("--------------------");
    }

    public void sendOrderCancelledEmail(Long orderId, String reason) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Invalid order ID for cancellation");
        }
        log.info("--- NOTIFICATION ---");
        log.info("To: customer@agrimarket.com");
        log.info("Subject: Order Cancelled");
        log.info("Body: Your order with ID {} has been cancelled. Reason: {}", orderId, reason);
        log.info("--------------------");
    }
}
