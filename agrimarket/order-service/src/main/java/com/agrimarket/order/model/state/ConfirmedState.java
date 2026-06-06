package com.agrimarket.order.model.state;

import com.agrimarket.order.model.Order;

public class ConfirmedState implements OrderState {

    @Override
    public void next(Order order) {
        throw new IllegalStateException("L'ordine è già CONFERMATO, non ci sono stati successivi.");
    }

    @Override
    public void cancel(Order order) {
        throw new IllegalStateException("Impossibile annullare un ordine già CONFERMATO.");
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.CONFIRMED;
    }
}
