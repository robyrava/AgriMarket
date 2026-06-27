package com.agrimarket.order.service;

import com.agrimarket.order.config.RabbitMQConfig;
import com.agrimarket.order.model.OutboxEvent;
import com.agrimarket.order.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import java.nio.charset.StandardCharsets;

import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPoller {

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${outbox.poller.delay:5000}")
    public void processOutboxEvents() {
        List<OutboxEvent> events = outboxEventRepository.findByProcessedFalse();
        for (OutboxEvent event : events) {
            try {
                log.info("Processing outbox event id: {}", event.getId());
                MessageProperties props = new MessageProperties();
                props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
                Message message = new Message(event.getPayload().getBytes(StandardCharsets.UTF_8), props);
                rabbitTemplate.send(RabbitMQConfig.EXCHANGE_NAME, event.getRoutingKey(), message);
                event.setProcessed(true);
                outboxEventRepository.save(event);
            } catch (Exception e) {
                log.error("Failed to process outbox event id: {}", event.getId(), e);
            }
        }
    }
}
