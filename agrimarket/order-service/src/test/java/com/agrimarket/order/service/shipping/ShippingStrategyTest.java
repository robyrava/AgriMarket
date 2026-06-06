package com.agrimarket.order.service.shipping;

import com.agrimarket.order.model.Order;
import com.agrimarket.order.model.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShippingStrategyTest {

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = Order.builder()
                .items(Arrays.asList(
                        OrderItem.builder().build(),
                        OrderItem.builder().build()
                )) // 2 items
                .build();
    }

    @Test
    void testStandardShippingStrategy() {
        ShippingStrategy strategy = new StandardShippingStrategy();
        // Base 5.00 + (2 items * 1.00) = 7.00
        assertEquals(7.00, strategy.calculateCost(testOrder));
    }

    @Test
    void testRefrigeratedShippingStrategy() {
        ShippingStrategy strategy = new RefrigeratedShippingStrategy();
        // Base 15.00 + (2 items * 2.50) = 20.00
        assertEquals(20.00, strategy.calculateCost(testOrder));
    }

    @Test
    void testPickupStrategy() {
        ShippingStrategy strategy = new PickupStrategy();
        assertEquals(0.0, strategy.calculateCost(testOrder));
    }
}
