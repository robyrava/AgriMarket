package com.agrimarket.order.model.state;

import com.agrimarket.order.model.Order;
import com.agrimarket.order.model.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderStateTest {

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        // The init() method is normally called by JPA @PostLoad, 
        // we'll simulate the Builder setting up the initial state or call init() manually
        order.setStato(OrderStatus.PENDING);
        order.setState(new PendingState());
        
        OrderItem item = new OrderItem();
        order.setItems(List.of(item));
    }

    @Test
    void testInitialStateIsPending() {
        assertEquals(OrderStatus.PENDING, order.getStato());
        assertTrue(order.getCurrentState() instanceof PendingState);
    }

    @Test
    void testPendingToReservedTransition() {
        order.nextState();
        assertEquals(OrderStatus.RESERVED, order.getStato());
        assertTrue(order.getCurrentState() instanceof ReservedState);
    }

    @Test
    void testReservedToConfirmedTransition() {
        order.nextState(); // PENDING -> RESERVED
        order.nextState(); // RESERVED -> CONFIRMED
        assertEquals(OrderStatus.CONFIRMED, order.getStato());
        assertTrue(order.getCurrentState() instanceof ConfirmedState);
    }

    @Test
    void testConfirmedCannotTransitionFurther() {
        order.nextState(); // -> RESERVED
        order.nextState(); // -> CONFIRMED
        assertThrows(IllegalStateException.class, () -> order.nextState());
    }

    @Test
    void testCannotCancelPendingOrderWithItems() {
        assertThrows(IllegalStateException.class, () -> order.cancel());
    }

    @Test
    void testCanCancelPendingOrderWithoutItems() {
        order.setItems(Collections.emptyList());
        order.cancel();
        assertEquals(OrderStatus.CANCELLED, order.getStato());
        assertTrue(order.getCurrentState() instanceof CancelledState);
    }

    @Test
    void testReservedToCancelledTransition() {
        order.nextState(); // PENDING -> RESERVED
        order.cancel();
        assertEquals(OrderStatus.CANCELLED, order.getStato());
        assertTrue(order.getCurrentState() instanceof CancelledState);
    }

    @Test
    void testCannotCancelConfirmedOrder() {
        order.nextState(); // PENDING -> RESERVED
        order.nextState(); // RESERVED -> CONFIRMED
        assertThrows(IllegalStateException.class, () -> order.cancel());
    }
}
