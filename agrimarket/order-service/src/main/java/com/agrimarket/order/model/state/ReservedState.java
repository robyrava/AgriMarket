package com.agrimarket.order.model.state;

import com.agrimarket.order.model.Order;

public class ReservedState implements OrderState {

    @Override
    public void next(Order order) {
        order.setState(new ConfirmedState());
    }

    @Override
    public void cancel(Order order) {
        order.setState(new CancelledState());
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.RESERVED;
    }
}
