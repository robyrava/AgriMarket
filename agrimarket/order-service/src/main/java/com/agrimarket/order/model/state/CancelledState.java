package com.agrimarket.order.model.state;

import com.agrimarket.order.model.Order;

public class CancelledState implements OrderState {

    @Override
    public void next(Order order) {
        throw new IllegalStateException("L'ordine è ANNULLATO, non può avanzare di stato.");
    }

    @Override
    public void cancel(Order order) {
        throw new IllegalStateException("L'ordine è già ANNULLATO.");
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.CANCELLED;
    }
}
