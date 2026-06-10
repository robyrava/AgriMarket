package com.agrimarket.notification.consumer;

import com.agrimarket.notification.event.OrderCancelledEvent;
import com.agrimarket.notification.event.OrderConfirmedEvent;
import com.agrimarket.notification.service.EmailSimulatorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final EmailSimulatorService emailSimulatorService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "${rabbitmq.queue.notification}")
    public void handleOrderEvent(Message message) throws Exception {
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        log.info("Received message in Notification Service with routing key: {}", routingKey);
        
        if ("order.confirmed".equals(routingKey)) {
            OrderConfirmedEvent event = objectMapper.readValue(message.getBody(), OrderConfirmedEvent.class);
            emailSimulatorService.sendOrderConfirmedEmail(event.getOrderId());
        } else if ("order.cancelled".equals(routingKey)) {
            OrderCancelledEvent event = objectMapper.readValue(message.getBody(), OrderCancelledEvent.class);
            emailSimulatorService.sendOrderCancelledEmail(event.getOrderId(), event.getReason());
        } else {
            log.warn("Received unknown routing key: {}", routingKey);
            throw new IllegalArgumentException("Unknown routing key: " + routingKey);
        }
    }
}
