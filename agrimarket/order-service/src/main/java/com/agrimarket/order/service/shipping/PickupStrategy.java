package com.agrimarket.order.service.shipping;

import com.agrimarket.order.model.Order;
import org.springframework.stereotype.Component;

@Component("PICKUP")
public class PickupStrategy implements ShippingStrategy {
    @Override
    public double calculateCost(Order order) {
        return 0.0; // Free pickup
    }
}
