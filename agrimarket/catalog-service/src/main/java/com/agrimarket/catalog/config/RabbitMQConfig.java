package com.agrimarket.catalog.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String INVENTORY_EXCHANGE = "inventory.exchange";
    public static final String INVENTORY_RESERVED_QUEUE = "inventory.reserved.queue";
    public static final String INVENTORY_FAILED_QUEUE = "inventory.failed.queue";

    public static final String INVENTORY_RESERVED_KEY = "inventory.reserved.key";
    public static final String INVENTORY_FAILED_KEY = "inventory.failed.key";

    @Bean
    public TopicExchange inventoryExchange() {
        return new TopicExchange(INVENTORY_EXCHANGE);
    }

    @Bean
    public Queue inventoryReservedQueue() {
        return new Queue(INVENTORY_RESERVED_QUEUE, true);
    }

    @Bean
    public Queue inventoryFailedQueue() {
        return new Queue(INVENTORY_FAILED_QUEUE, true);
    }

    @Bean
    public Binding bindingInventoryReserved() {
        return BindingBuilder.bind(inventoryReservedQueue()).to(inventoryExchange()).with(INVENTORY_RESERVED_KEY);
    }

    @Bean
    public Binding bindingInventoryFailed() {
        return BindingBuilder.bind(inventoryFailedQueue()).to(inventoryExchange()).with(INVENTORY_FAILED_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
