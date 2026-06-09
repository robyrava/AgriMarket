package com.agrimarket.catalog.messaging;

import com.agrimarket.catalog.config.RabbitMQConfig;
import com.agrimarket.catalog.dto.InventoryFailedEvent;
import com.agrimarket.catalog.dto.InventoryReservedEvent;
import com.agrimarket.catalog.dto.OrderCreatedEvent;
import com.agrimarket.catalog.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final InventoryService inventoryService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "order.created.queue")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent for order id: {}", event.getOrderId());
        
        try {
            inventoryService.reserveInventory(event.getItems());
            
            InventoryReservedEvent reservedEvent = new InventoryReservedEvent(event.getOrderId());
            rabbitTemplate.convertAndSend(RabbitMQConfig.INVENTORY_EXCHANGE, RabbitMQConfig.INVENTORY_RESERVED_KEY, reservedEvent);
            log.info("Published InventoryReservedEvent for order id: {}", event.getOrderId());
            
        } catch (Exception e) {
            log.error("Failed to reserve inventory for order id: {}", event.getOrderId(), e);
            
            InventoryFailedEvent failedEvent = new InventoryFailedEvent(event.getOrderId(), e.getMessage());
            rabbitTemplate.convertAndSend(RabbitMQConfig.INVENTORY_EXCHANGE, RabbitMQConfig.INVENTORY_FAILED_KEY, failedEvent);
            log.info("Published InventoryFailedEvent for order id: {}", event.getOrderId());
        }
    }
}
