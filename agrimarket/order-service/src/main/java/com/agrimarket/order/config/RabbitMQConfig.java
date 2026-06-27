package com.agrimarket.order.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "order.exchange";
    public static final String QUEUE_NAME = "order.created.queue";
    public static final String ROUTING_KEY_CONFIRMED = "order.confirmed";
    public static final String ROUTING_KEY_CANCELLED = "order.cancelled";

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue orderQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Binding confirmedBinding(Queue orderQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderQueue).to(orderExchange).with(ROUTING_KEY_CONFIRMED);
    }

    @Bean
    public Binding cancelledBinding(Queue orderQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderQueue).to(orderExchange).with(ROUTING_KEY_CANCELLED);
    }

    @Bean
    public org.springframework.amqp.support.converter.MessageConverter messageConverter() {
        return new org.springframework.amqp.support.converter.Jackson2JsonMessageConverter();
    }
}
