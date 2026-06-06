package com.agrimarket.order.model.state;

import com.agrimarket.order.model.Order;

public class PendingState implements OrderState {

    @Override
    public void next(Order order) {
        order.setState(new ReservedState());
    }

    @Override
    public void cancel(Order order) {
        // Specific rule: cannot cancel from PENDING unless it's an empty order
        if (order.getItems() == null || order.getItems().isEmpty()) {
            order.setState(new CancelledState());
        } else {
            throw new IllegalStateException("Impossibile annullare un ordine in stato PENDING con articoli presenti. Procedere prima alla reservation.");
        }
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.PENDING;
    }
}
