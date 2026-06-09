package com.agrimarket.order.model.state;

import com.agrimarket.order.model.Order;

public class PendingState implements OrderState {

    @Override
    public void next(Order order) {
        order.setState(new ReservedState());
    }

    @Override
    public void cancel(Order order) {
        order.setState(new CancelledState());
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.PENDING;
    }
}
