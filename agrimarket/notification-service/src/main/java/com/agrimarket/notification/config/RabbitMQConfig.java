package com.agrimarket.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.order}")
    private String orderExchangeName;

    @Value("${rabbitmq.queue.notification}")
    private String notificationQueueName;

    @Value("${rabbitmq.queue.dlq}")
    private String dlqName;

    @Value("${rabbitmq.routing-key.order-confirmed}")
    private String orderConfirmedRoutingKey;

    @Value("${rabbitmq.routing-key.order-cancelled}")
    private String orderCancelledRoutingKey;

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(orderExchangeName);
    }

    @Bean
    public Queue dlq() {
        return QueueBuilder.durable(dlqName).build();
    }

    @Bean
    public DirectExchange dlx() {
        return new DirectExchange(dlqName + ".exchange");
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(dlq()).to(dlx()).with(dlqName + ".routing-key");
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(notificationQueueName)
                .withArgument("x-dead-letter-exchange", dlx().getName())
                .withArgument("x-dead-letter-routing-key", dlqName + ".routing-key")
                .build();
    }

    @Bean
    public Binding confirmedBinding(Queue notificationQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(notificationQueue).to(orderExchange).with(orderConfirmedRoutingKey);
    }

    @Bean
    public Binding cancelledBinding(Queue notificationQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(notificationQueue).to(orderExchange).with(orderCancelledRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
