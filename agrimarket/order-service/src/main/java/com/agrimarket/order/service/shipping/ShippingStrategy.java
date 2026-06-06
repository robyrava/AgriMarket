package com.agrimarket.order.service.shipping;

import com.agrimarket.order.model.Order;

public interface ShippingStrategy {
    double calculateCost(Order order);
}
