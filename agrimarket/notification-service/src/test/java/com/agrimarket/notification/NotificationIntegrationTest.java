package com.agrimarket.notification;

import com.agrimarket.notification.consumer.NotificationEventConsumer;
import com.agrimarket.notification.event.OrderCancelledEvent;
import com.agrimarket.notification.event.OrderConfirmedEvent;
import com.agrimarket.notification.service.EmailSimulatorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;

@SpringBootTest
public class NotificationIntegrationTest {

    @MockitoBean
    private ConnectionFactory connectionFactory;

    @Autowired
    private NotificationEventConsumer notificationEventConsumer;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmailSimulatorService emailSimulatorService;

    @Test
    public void testOrderConfirmedEvent_SendsNotification() throws Exception {
        OrderConfirmedEvent event = new OrderConfirmedEvent(101L);
        byte[] body = objectMapper.writeValueAsBytes(event);
        MessageProperties props = new MessageProperties();
        props.setContentType("application/json");
        props.setReceivedRoutingKey("order.confirmed");
        Message message = new Message(body, props);

        notificationEventConsumer.handleOrderEvent(message);

        verify(emailSimulatorService).sendOrderConfirmedEmail(eq(101L));
    }

    @Test
    public void testOrderCancelledEvent_SendsNotification() throws Exception {
        OrderCancelledEvent event = new OrderCancelledEvent(102L, "Out of stock");
        byte[] body = objectMapper.writeValueAsBytes(event);
        MessageProperties props = new MessageProperties();
        props.setContentType("application/json");
        props.setReceivedRoutingKey("order.cancelled");
        Message message = new Message(body, props);

        notificationEventConsumer.handleOrderEvent(message);

        verify(emailSimulatorService).sendOrderCancelledEmail(eq(102L), eq("Out of stock"));
    }

    @Test
    public void testInvalidMessage_ThrowsExceptionForDLQ() throws Exception {
        byte[] body = "invalid-json".getBytes();
        MessageProperties props = new MessageProperties();
        props.setContentType("application/json");
        props.setReceivedRoutingKey("order.unknown");
        Message message = new Message(body, props);

        // This simulates the DLQ behavior: since it throws IllegalArgumentException and requeue is false,
        // it will be sent to the DLQ.
        assertThrows(IllegalArgumentException.class, () -> {
            notificationEventConsumer.handleOrderEvent(message);
        });
    }
}
