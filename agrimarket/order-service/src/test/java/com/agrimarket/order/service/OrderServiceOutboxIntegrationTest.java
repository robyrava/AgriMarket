package com.agrimarket.order.service;

import com.agrimarket.order.config.RabbitMQConfig;
import com.agrimarket.order.dto.OrderCreateRequest;
import com.agrimarket.order.dto.OrderItemRequest;
import com.agrimarket.order.model.Order;
import com.agrimarket.order.model.OutboxEvent;
import com.agrimarket.order.repository.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:order_db_test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "outbox.poller.delay=9999999" // Prevent automatic polling during test to control manually
})
class OrderServiceOutboxIntegrationTest {

    @MockitoBean
    private ConnectionFactory connectionFactory;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private OutboxPoller outboxPoller;

    @Test
    void shouldSaveOutboxEventAndPublishToRabbitMQ() {
        // 1. Create Order
        OrderCreateRequest request = new OrderCreateRequest();
        request.setCustomerId(100L);
        request.setTipoSpedizione("STANDARD");
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantita(2);
        item.setPrezzo(10.5);
        request.setItems(List.of(item));

        Order savedOrder = orderService.createOrder(request);

        assertNotNull(savedOrder.getId());

        // 2. Verify OutboxEvent is saved
        List<OutboxEvent> outboxEvents = outboxEventRepository.findAll();
        assertEquals(1, outboxEvents.size());
        OutboxEvent event = outboxEvents.get(0);
        assertEquals("OrderCreated", event.getType());
        assertEquals(savedOrder.getId().toString(), event.getAggregateId());
        assertFalse(event.isProcessed());

        // 3. Trigger Poller manually (or it might have run automatically)
        outboxPoller.processOutboxEvents();

        // 4. Verify message is published at least once
        verify(rabbitTemplate, org.mockito.Mockito.atLeast(1)).convertAndSend(eq(RabbitMQConfig.EXCHANGE_NAME), eq(RabbitMQConfig.ROUTING_KEY), anyString());

        // 5. Verify OutboxEvent is updated
        OutboxEvent processedEvent = outboxEventRepository.findById(event.getId()).get();
        assertTrue(processedEvent.isProcessed());
    }
}
