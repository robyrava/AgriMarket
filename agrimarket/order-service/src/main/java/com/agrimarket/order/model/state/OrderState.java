package com.agrimarket.order.model.state;

import com.agrimarket.order.model.Order;

public interface OrderState {
    void next(Order order);
    void cancel(Order order);
    OrderStatus getStatus();
}
