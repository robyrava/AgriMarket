package com.agrimarket.order.messaging;

import com.agrimarket.order.dto.InventoryFailedEvent;
import com.agrimarket.order.dto.InventoryReservedEvent;
import com.agrimarket.order.model.state.OrderStatus;
import com.agrimarket.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventConsumer {

    private final OrderService orderService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "inventory.reserved.queue", durable = "true"),
            exchange = @Exchange(value = "inventory.exchange", type = "topic"),
            key = "inventory.reserved.key"
    ))
    public void handleInventoryReserved(InventoryReservedEvent event) {
        log.info("Received InventoryReservedEvent for order id: {}", event.getOrderId());
        try {
            com.agrimarket.order.model.Order order = orderService.getOrder(event.getOrderId());
            // Transition to CONFIRMED using state pattern
            while (order.getStato() != OrderStatus.CONFIRMED && order.getStato() != OrderStatus.CANCELLED) {
                orderService.updateOrderState(event.getOrderId(), "next");
                order = orderService.getOrder(event.getOrderId());
            }
            log.info("Order id {} is now CONFIRMED", event.getOrderId());
        } catch (Exception e) {
            log.error("Error processing InventoryReservedEvent", e);
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "inventory.failed.queue", durable = "true"),
            exchange = @Exchange(value = "inventory.exchange", type = "topic"),
            key = "inventory.failed.key"
    ))
    public void handleInventoryFailed(InventoryFailedEvent event) {
        log.info("Received InventoryFailedEvent for order id: {} reason: {}", event.getOrderId(), event.getReason());
        try {
            orderService.updateOrderState(event.getOrderId(), "cancel");
            log.info("Order id {} is now CANCELLED (Compensation)", event.getOrderId());
        } catch (Exception e) {
            log.error("Error processing InventoryFailedEvent", e);
        }
    }
}
