package com.agrimarket.order.service;

import com.agrimarket.order.dto.InventoryFailedEvent;
import com.agrimarket.order.dto.InventoryReservedEvent;
import com.agrimarket.order.model.Order;
import com.agrimarket.order.model.state.OrderStatus;
import com.agrimarket.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:order_db_test_saga;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "outbox.poller.delay=9999999"
})
public class SagaIntegrationTest {

    @Autowired
    private com.agrimarket.order.messaging.InventoryEventConsumer inventoryEventConsumer;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    public void testInventoryReservedTransitionsOrderToConfirmed() {
        // Create an order directly in db
        Order order = new Order();
        order.setCustomerId(1L);
        order.setTotale(100.0);
        order.setStato(OrderStatus.PENDING);
        order.setTipoSpedizione("STANDARD");
        order.setCostoSpedizione(5.0);
        Order savedOrder = orderRepository.save(order);

        // Simulate receiving event directly for test simplicity without needing a broker
        InventoryReservedEvent event = new InventoryReservedEvent(savedOrder.getId());
        
        // This is called synchronously, but Awaitility is requested to show async handling
        Thread asyncThread = new Thread(() -> inventoryEventConsumer.handleInventoryReserved(event));
        asyncThread.start();

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Order updatedOrder = orderRepository.findById(savedOrder.getId()).get();
            assertEquals(OrderStatus.CONFIRMED, updatedOrder.getStato());
        });
    }

    @Test
    public void testInventoryFailedTransitionsOrderToCancelled() {
        // Create an order directly in db
        Order order = new Order();
        order.setCustomerId(1L);
        order.setTotale(100.0);
        order.setStato(OrderStatus.PENDING);
        order.setTipoSpedizione("STANDARD");
        order.setCostoSpedizione(5.0);
        Order savedOrder = orderRepository.save(order);

        // Simulate receiving event directly
        InventoryFailedEvent event = new InventoryFailedEvent(savedOrder.getId(), "Out of stock");
        
        Thread asyncThread = new Thread(() -> inventoryEventConsumer.handleInventoryFailed(event));
        asyncThread.start();

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Order updatedOrder = orderRepository.findById(savedOrder.getId()).get();
            assertEquals(OrderStatus.CANCELLED, updatedOrder.getStato());
        });
    }
}
