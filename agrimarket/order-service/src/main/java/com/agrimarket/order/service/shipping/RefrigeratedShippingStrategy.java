package com.agrimarket.order.service.shipping;

import com.agrimarket.order.model.Order;
import org.springframework.stereotype.Component;

@Component("REFRIGERATED")
public class RefrigeratedShippingStrategy implements ShippingStrategy {
    @Override
    public double calculateCost(Order order) {
        return 15.00 + (order.getItems().size() * 2.50); // Base cost 15 + 2.50 per item
    }
}
